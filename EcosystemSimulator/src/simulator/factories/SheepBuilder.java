package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Sheep;

public class SheepBuilder extends Builder<Animal> {

	private Factory<SelectionStrategy> _selection_strategy_factory;

	public SheepBuilder(Factory<SelectionStrategy> selection_strategy_factory) {

		super("sheep", "Sheep");
		_selection_strategy_factory = selection_strategy_factory;
	}

	@Override
	protected Animal create_instance(JSONObject data) {

		SelectionStrategy danger_strategy = new SelectFirst();
		SelectionStrategy mate_strategy = new SelectFirst();
		Vector2D pos = null;

		if (data.has("mate_strategy"))
			mate_strategy = _selection_strategy_factory.create_instance(data.getJSONObject("mate_strategy"));

		if (data.has("danger_strategy"))
			danger_strategy = _selection_strategy_factory.create_instance(data.getJSONObject("danger_strategy"));

		if (data.has("pos")) {

			JSONObject pos_jo = data.getJSONObject("pos");
			if (!pos_jo.has("x_range"))
				throw new IllegalArgumentException("x_range key is missing in pos");
			if (!pos_jo.has("y_range"))
				throw new IllegalArgumentException("y_range key is missing in pos");

			JSONArray x_range = pos_jo.getJSONArray("x_range");
			if (x_range.length() != 2)
				throw new IllegalArgumentException("x_range array has to have a length of 2");

			JSONArray y_range = pos_jo.getJSONArray("y_range");
			if (y_range.length() != 2)
				throw new IllegalArgumentException("y_range array has to have a length of 2");

			double x = Utils._rand.nextDouble(x_range.getDouble(0), x_range.getDouble(1));
			double y = Utils._rand.nextDouble(y_range.getDouble(0), y_range.getDouble(1));

			pos = new Vector2D(x, y);

		}

		return new Sheep(mate_strategy, danger_strategy, pos);
	}

	@Override
	protected void fill_in_data(JSONObject o) {

		o.put("mate strategy", "strategy followed to mate (optional, default first)");
		o.put("danger strategy", "strategy followed to choose a danger (optional, default first)");
		o.put("pos", "initial position");

	}

}
