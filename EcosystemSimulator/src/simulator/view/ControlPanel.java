package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.json.JSONObject;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import simulator.control.Controller;
import simulator.launcher.Main;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel {

	private Controller _ctrl;
	private ChangeRegionsDialog _changeRegionsDialog;
	private JToolBar _toolaBar;
	private JFileChooser _fc;

	private boolean _stopped = true; // utilizado en los botones de run/stop
	private JButton _quitButton;

	private JButton _openButton;
	private JButton _viewerButton;
	private JButton _regionsButton;
	private JButton _runButton;
	private JButton _stopButton;
	private JSpinner _stepsSpinner;
	private JTextField _dtTextField;

	ControlPanel(Controller ctrl) {
		_ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		_toolaBar = new JToolBar();
		add(_toolaBar, BorderLayout.PAGE_START);

		// botones
		start_open_button();
		start_viewer_button();
		start_regions_button();
		start_run_button();
		start_stop_button();
		start_steps_spinner();
		start_dt_textField();
		start_quit_button();

		_fc = new JFileChooser();
		_fc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/resources/examples"));

		_changeRegionsDialog = new ChangeRegionsDialog(_ctrl);
	}

	private void start_quit_button() {

		_toolaBar.add(Box.createGlue());
		_toolaBar.addSeparator();
		_quitButton = new JButton();
		_quitButton.setToolTipText("Quit");
		_quitButton.setIcon(new ImageIcon("resources/icons/exit.png"));
		_quitButton.addActionListener((e) -> ViewUtils.quit(this));
		_toolaBar.add(_quitButton);
	}

	private void start_open_button() {

		_openButton = new JButton();
		_openButton.setToolTipText("Load an input file into the simulator");
		_openButton.setIcon(new ImageIcon("resources/icons/open.png"));

		_openButton.addActionListener((e) -> {

			int option = _fc.showOpenDialog(ViewUtils.getWindow(this));
			if (option == JFileChooser.APPROVE_OPTION) {
				
				try {
					InputStream is = new FileInputStream(_fc.getSelectedFile());
					JSONObject jo = Main.load_JSON_file(is);
					
					_ctrl.reset(jo.getInt("cols"), jo.getInt("rows"), jo.getInt("width"), jo.getInt("height"));
					_ctrl.load_data(jo);
					
				} catch (FileNotFoundException error) {
					ViewUtils.showErrorMsg(error.getMessage());
				}

			}
		});

		_toolaBar.add(_openButton);
	}

	private void start_viewer_button() {

		_toolaBar.addSeparator();
		_viewerButton = new JButton();
		_viewerButton.setToolTipText("Map Viewer");
		_viewerButton.setIcon(new ImageIcon("resources/icons/viewer.png"));
		_viewerButton.addActionListener((e) -> new MapWindow(new JFrame(), _ctrl));
		_toolaBar.add(_viewerButton);
	}

	private void start_regions_button() {

		_regionsButton = new JButton();
		_regionsButton.setToolTipText("Change Regions");
		_regionsButton.setIcon(new ImageIcon("resources/icons/regions.png"));
		_regionsButton.addActionListener((e) -> _changeRegionsDialog.open(ViewUtils.getWindow(this)));
		_toolaBar.add(_regionsButton);
	}

	private void start_run_button() {

		_toolaBar.addSeparator();
		_runButton = new JButton();
		_runButton.setToolTipText("Run the simulator");
		_runButton.setIcon(new ImageIcon("resources/icons/run.png"));

		_runButton.addActionListener((e) -> {

			switch_buttons(false);
			_stopped = false;
			double dt = Double.parseDouble(_dtTextField.getText());
			int n = (Integer) _stepsSpinner.getValue();
			run_sim(n, dt);
			
		});

		_toolaBar.add(_runButton);
	}

	private void run_sim(int n, double dt) {
		
		if (n > 0 && !_stopped) {
			try {
				long startTime = System.currentTimeMillis(); 
				_ctrl.advance(dt); 
				long stepTimeMs = System.currentTimeMillis() - startTime; 
				long delay = (long) (dt * 1000 - stepTimeMs); 
				Thread.sleep(delay > 0 ? delay : 0);
				_ctrl.advance(dt);
				SwingUtilities.invokeLater(() -> run_sim(n - 1, dt));
			} catch (Exception e) {
				ViewUtils.showErrorMsg(e.getMessage());
				switch_buttons(true);
				_stopped = true;
			}
		} else {
			switch_buttons(true);
			_stopped = true;
		}
	}

	private void switch_buttons(boolean b) {

		_openButton.setEnabled(b);
		_regionsButton.setEnabled(b);
		_viewerButton.setEnabled(b);
		_quitButton.setEnabled(b);
		_dtTextField.setEnabled(b);
		_stepsSpinner.setEnabled(b);
		_runButton.setEnabled(b);
	}

	private void start_stop_button() {

		_stopButton = new JButton();
		_stopButton.setToolTipText("Stop the simulator");
		_stopButton.setIcon(new ImageIcon("resources/icons/stop.png"));
		_stopButton.addActionListener((e) -> _stopped = true);
		_toolaBar.add(_stopButton);
	}

	private void start_steps_spinner() {

		_toolaBar.addSeparator();
		_stepsSpinner = new JSpinner(new SpinnerNumberModel(10000, 1, 10000, 100));
		_stepsSpinner.setToolTipText("Simulation steps to run: 1-10000");
		_stepsSpinner.setMaximumSize(new Dimension(70, 40));
		_stepsSpinner.setMinimumSize(new Dimension(70, 40));
		_stepsSpinner.setPreferredSize(new Dimension(70, 40));
		_toolaBar.add(new JLabel("Steps: "));
		_toolaBar.add(_stepsSpinner);
	}

	private void start_dt_textField() {

		_toolaBar.addSeparator();
		_dtTextField = new JTextField(Main._delta_time.toString());
		_dtTextField.setToolTipText("Real time (seconds) corresponding to a step");
		_dtTextField.setFont(_stepsSpinner.getFont());
		_dtTextField.setMaximumSize(new Dimension(70, 40));
		_dtTextField.setMinimumSize(new Dimension(70, 40));
		_dtTextField.setPreferredSize(new Dimension(70, 40));
		_toolaBar.add(new JLabel("Delta-Time: "));
		_toolaBar.add(_dtTextField);
	}
}