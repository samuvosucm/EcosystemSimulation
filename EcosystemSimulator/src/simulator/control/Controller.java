package simulator.control;

import java.io.OutputStream;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.Simulator;

public class Controller {

	private Simulator _sim;

	public Controller(Simulator sim) {
		_sim = sim;
	}

	public void load_data(JSONObject data) {

		if (data.has("regions"))
			load_regions(data.getJSONArray("regions"));

		load_animals(data.getJSONArray("animals"));
	}

	private void load_animals(JSONArray animals) {

		for (int animal = 0; animal < animals.length(); animal++) {

			JSONObject ja = animals.getJSONObject(animal);

			JSONObject O = ja.getJSONObject("spec");
			int N = ja.getInt("amount");

			for (int n = 0; n < N; n++)
				_sim.add_animal(O);
		}
	}

	private void load_regions(JSONArray regions) {

		for (int region = 0; region < regions.length(); region++) {

			set_regions(regions.getJSONObject(region));
		}
	}

	public void run(double t, double dt, boolean sv, OutputStream out) {

		SimpleObjectViewer view = null;
		JSONObject jo1 = new JSONObject();

		if (sv) {
			MapInfo m = _sim.get_map_info();
			view = new SimpleObjectViewer("[ECOSYSTEM]", m.get_width(), m.get_height(), m.get_cols(), m.get_rows());
			view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);
		}

		jo1.put("in", _sim.as_JSON());
		while (this._sim.get_time() < t) {
			advance(dt);
			if (sv)
				view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);
		}
		jo1.put("out", _sim.as_JSON());

		PrintStream p = new PrintStream(out);
		p.println(jo1.toString(2));

		if (sv)
			view.close();
	}

	private List<ObjInfo> to_animals_info(List<? extends AnimalInfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		for (AnimalInfo a : animals)
			ol.add(new ObjInfo(a.get_genetic_code(), (int) a.get_position().getX(), (int) a.get_position().getY(),
					(int) Math.round(a.get_age()) + 2));
		return ol;
	}
	
	public void reset(int cols, int rows, int width, int height) {
		_sim.reset(cols, rows, width, height);
	}
	
	public void set_regions(JSONObject rs) {
		
		JSONArray row = rs.getJSONArray("row");
		JSONArray col = rs.getJSONArray("col");
		JSONObject O = rs.getJSONObject("spec");

		for (int R = row.getInt(0); R <= row.getInt(1); R++)
			for (int C = col.getInt(0); C <= col.getInt(1); C++) {
				_sim.set_region(R, C, O);
			}
	}
	
	public void advance(double dt) {
		_sim.advance(dt);
	}
	
	public void add_observer(EcoSysObserver o) {
		_sim.add_observer(o);
	}
	
	public void remove_observer(EcoSysObserver o) {
		_sim.remove_observer(o);
	}
}
