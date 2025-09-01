package simulator.model;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {

	protected List<Animal> _animals;

	public Region() {

		_animals = new ArrayList<>();
	}

	final void add_animal(Animal a) {
		_animals.add(a);
	}

	final void remove_animal(Animal a) {
		_animals.remove(a);
	}

	public List<Animal> get_animals() {
		return Collections.unmodifiableList(_animals);
	}
	
	public List<AnimalInfo> get_animals_info() {
		return new ArrayList<>(_animals);
	}

	public JSONObject as_JSON() {

		JSONArray ja = new JSONArray();

		for (Animal a : _animals)
			ja.put(a.as_JSON());

		return new JSONObject().put("animals", ja);
	}
}
