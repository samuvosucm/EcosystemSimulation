package simulator.factories;

import org.json.JSONObject;

import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic food supply");
	}

	@Override
	protected Region create_instance(JSONObject data) {

		double factor = DynamicSupplyRegion.DEFAULT_FACTOR;
		double food = DynamicSupplyRegion.DEFAULT_FOOD;

		if (data.has("factor"))
			factor = data.getDouble("factor");

		if (data.has("food"))
			food = data.getDouble("food");

		return new DynamicSupplyRegion(food, factor);
	}

	@Override
	protected void fill_in_data(JSONObject o) {

		o.put("factor", "food increase factor (optional, default " + DynamicSupplyRegion.DEFAULT_FACTOR + ")");
		o.put("food", "intial amount of food (optional, default " + DynamicSupplyRegion.DEFAULT_FOOD + ")");
	}
}
