package simulator.launcher;

import java.io.File;
import simulator.view.CarViewer;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import simulator.factories.*;
import simulator.model.Animal;
import simulator.model.Region;
import simulator.model.SelectionStrategy;
import simulator.misc.Utils;
import java.util.ArrayList;
import java.util.List;
import simulator.view.NoViewer;

import javax.swing.SwingUtilities;

import simulator.model.Simulator;
import simulator.view.MainWindow;
import simulator.control.Controller;

public class Main {

	private enum ExecMode {
		BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

		private String _tag;
		private String _desc;

		private ExecMode(String modeTag, String modeDesc) {
			_tag = modeTag;
			_desc = modeDesc;
		}

		public String get_tag() {
			return _tag;
		}

		public String get_desc() {
			return _desc;
		}
	}

	// default values for some parameters
	//
	private final static Double _default_time = 10.0; // in seconds
	private final static Double _default_delta_time = 0.003;

	// some attributes to stores values corresponding to command-line parameters
	//
	private static Double _time = null;
	private static String _in_file = null;
	private static ExecMode _mode = ExecMode.GUI;
	private static String _out_file = null;
	private static Boolean _sv = false;
	private static Boolean _car = false;
	private static Boolean _no = false;
	public static Double _delta_time = null;

	// factories
	//
	public static Factory<SelectionStrategy> _selection_strategy_factory;
	public static Factory<Animal> _animals_factory;
	public static Factory<Region> _regions_factory;

	private static void parse_args(String[] args) {

		// define the valid command line options
		//
		Options cmdLineOptions = build_options();

		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parse_execution_mode_option(line);
			parse_help_option(line, cmdLineOptions);
			parse_in_file_option(line);
			parse_sv_option(line);
			parse_car_option(line);
			parse_no_option(line);
			parse_dt_option(line);
			parse_out_file_option(line);
			parse_time_option(line);


			// if there are some remaining arguments, then something wrong is
			// provided in the command line!
			//
			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}

		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	private static Options build_options() {
		Options cmdLineOptions = new Options();

		// delta time
		cmdLineOptions
				.addOption(
						Option.builder("dt").longOpt("delta-time").hasArg()
								.desc("A double representing actual time, in\n"
										+ "seconds, per simulation step. Default value: " + _default_delta_time + ".")
								.build());

		// help
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

		// input file
		cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("A configuration file (optional in GUI mode).").build());

		// execution mode
		cmdLineOptions.addOption(Option.builder("m").longOpt("mode").hasArg().desc("Execution Mode. Possible values: 'batch' (Batch"
				+ "mode), 'gui' (Graphical User Interface mode). Default value: '" + _mode + "'.").build());
		
		
		// output file
		cmdLineOptions.addOption(
				Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is writte (only for BATCH mode).").build());

		// simple viewer
		cmdLineOptions.addOption(
				Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in BATCH mode.").build());

		// car option 
		cmdLineOptions.addOption(
				Option.builder("car").longOpt("car-option").desc("Show number of steps for each region in which number of "
						+ "carnivores is greater than 3.").build());

		// no option 
				cmdLineOptions.addOption(
						Option.builder("no").longOpt("no-option").desc("Show number of animals who have moved to the north.").build());

		// steps
		cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
				.desc("An real number representing the total simulation time in seconds. Default value: "
						+ _default_time + ". (only for BATCH mode")
				.build());

		return cmdLineOptions;
	}

	private static void parse_help_option(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	private static void parse_in_file_option(CommandLine line) throws ParseException {
		_in_file = line.getOptionValue("i");
		if (_mode == ExecMode.BATCH && _in_file == null) {
			throw new ParseException("In batch mode an input configuration file is required");
		}
	}

	private static void parse_time_option(CommandLine line) throws ParseException {
		String t = line.getOptionValue("t", _default_time.toString());
		try {
			_time = Double.parseDouble(t);
			assert (_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + t);
		}
	}

	private static void parse_sv_option(CommandLine line) {
		if (line.hasOption("sv"))
			_sv = true;
	}
	
	private static void parse_no_option(CommandLine line) {
		if (line.hasOption("no"))
			_no = true;
	}
	
	private static void parse_car_option(CommandLine line) {
		if (line.hasOption("car"))
			_car = true;
	}

	private static void parse_dt_option(CommandLine line) throws ParseException {
		String dt = line.getOptionValue("dt", _default_delta_time.toString());
		try {
			_delta_time = Double.parseDouble(dt);
			assert (_delta_time >= 0 && _delta_time < _time);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + dt);
		}
	}

	private static void parse_out_file_option(CommandLine line) throws ParseException {
		_out_file = line.getOptionValue("o");
		if (_mode == ExecMode.BATCH && _out_file == null) {
			throw new ParseException("In batch mode an output configuration file is required");
		}
	}
	
	private static void parse_execution_mode_option(CommandLine line) {
		_out_file = line.getOptionValue("m", _mode.toString()).toUpperCase();
		if (_out_file.equals(ExecMode.BATCH.toString()))
			_mode = ExecMode.BATCH;
		else if (_out_file.equals(ExecMode.GUI.name()))
			_mode = ExecMode.GUI;
	}

	private static void init_factories() {

		// initialize the strategies factory
		List<Builder<SelectionStrategy>> selection_strategy_builders = new ArrayList<>();
		selection_strategy_builders.add(new SelectFirstBuilder());
		selection_strategy_builders.add(new SelectClosestBuilder());
		selection_strategy_builders.add(new SelectYoungestBuilder());
		_selection_strategy_factory = new BuilderBasedFactory<SelectionStrategy>(selection_strategy_builders);

		// initialize animals factory
		List<Builder<Animal>> animal_builder = new ArrayList<>();
		animal_builder.add(new SheepBuilder(_selection_strategy_factory));
		animal_builder.add(new WolfBuilder(_selection_strategy_factory));
		_animals_factory = new BuilderBasedFactory<Animal>(animal_builder);

		// initialize regions factory
		List<Builder<Region>> region_builder = new ArrayList<>();
		region_builder.add(new DefaultRegionBuilder());
		region_builder.add(new DynamicSupplyRegionBuilder());
		_regions_factory = new BuilderBasedFactory<Region>(region_builder);

	}

	public static JSONObject load_JSON_file(InputStream in) {
		return new JSONObject(new JSONTokener(in));
	}

	private static void start_batch_mode() throws Exception {
		InputStream is = new FileInputStream(new File(_in_file));
		JSONObject jo = load_JSON_file(is);
		OutputStream os = new FileOutputStream(new File(_out_file));

		Simulator sim = new Simulator(jo.getInt("cols"), jo.getInt("rows"), jo.getInt("width"), jo.getInt("height"),
				_animals_factory, _regions_factory);
		Controller controller = new Controller(sim);
		CarViewer car = new CarViewer(controller);
		controller.load_data(jo);
		if (_no) {
			NoViewer no = new NoViewer(controller);	
		}
		controller.run(_time, _delta_time, _sv, os);
		
		if (_car)
			car.imprimir();
		

		
		os.close();
	}

	private static void start_GUI_mode() throws Exception {
		
		Controller controller;
		if (_in_file != null) {
			InputStream is = new FileInputStream(new File(_in_file));
			JSONObject jo = load_JSON_file(is);
			Simulator sim = new Simulator(jo.getInt("cols"), jo.getInt("rows"), jo.getInt("width"), jo.getInt("height"),
					_animals_factory, _regions_factory);
			controller = new Controller(sim);
			controller.load_data(jo);
		}
		else {
			Simulator sim = new Simulator(20, 15, 800, 600, _animals_factory, _regions_factory);
			controller = new Controller(sim);
		}
		SwingUtilities.invokeAndWait(() -> new MainWindow(controller));

	}

	private static void start(String[] args) throws Exception {
		init_factories();
		parse_args(args);
		switch (_mode) {
		case BATCH:
			start_batch_mode();
			break;
		case GUI:
			start_GUI_mode();
			break;
		}
	}

	public static void main(String[] args) {
		Utils._rand.setSeed(2147483647l);
		try {
			start(args);
		} catch (Exception e) {
			System.err.println("Something went wrong ...");
			System.err.println();
			e.printStackTrace();
		}
	}
}
