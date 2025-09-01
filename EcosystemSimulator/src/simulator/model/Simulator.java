package simulator.model;

import java.util.List;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import simulator.factories.Factory;

public class Simulator implements JSONable, Observable<EcoSysObserver> {

	private Factory<Animal> _animals_factory;
	private Factory<Region> _regions_factory;
	private RegionManager _region_mngr;
	private List<Animal> _animals;
	private List<EcoSysObserver> _observers;
	private double _time;

	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animals_factory,
			Factory<Region> regions_factory) {

		_animals_factory = animals_factory;
		_regions_factory = regions_factory;
		_region_mngr = new RegionManager(cols, rows, width, height);
		_animals = new ArrayList<>();
		_time = 0.0;
		_observers = new ArrayList<>();
	}

	private void set_region(int row, int col, Region r) {
		_region_mngr.set_region(row, col, r);
	}

	public void set_region(int row, int col, JSONObject r_json) {

		Region r = _regions_factory.create_instance(r_json);
		set_region(row, col, r);
		
		notify_on_region_set(row, col, r);
	}

	private void add_animal(Animal a) {

		_animals.add(a);
		_region_mngr.register_animal(a);
	}

	public void add_animal(JSONObject a_json) {

		Animal a = _animals_factory.create_instance(a_json);
		add_animal(a);
		
		notify_on_animal_added(a);
	}

	public MapInfo get_map_info() {
		return _region_mngr;
	}

	public List<? extends AnimalInfo> get_animals() {
		return Collections.unmodifiableList(_animals);
	}

	public double get_time() {
		return _time;
	}

	public void advance(double dt) {

		_time += dt;

		Iterator<Animal> it = _animals.iterator();
		while (it.hasNext()) {

			Animal animal = it.next();
			if (animal.get_state().equals(State.DEAD)) {
				_region_mngr.unregister_animal(animal);
				it.remove();
			}
		}

		for (Animal a : _animals) {

			a.update(dt);
			_region_mngr.update_animal_region(a);
		}

		_region_mngr.update_all_regions(dt);

		int n = _animals.size();
		for (int animal = 0; animal < n; animal++) {

			Animal a = _animals.get(animal);
			if (a.is_pregnant())
				add_animal(a.deliver_baby());
		}
		
		notify_on_advanced(dt);
	}

	public JSONObject as_JSON() {

		JSONObject jo = new JSONObject();

		jo.put("time", _time);
		jo.put("state", _region_mngr.as_JSON());

		return jo;
	}
	
	public void reset(int cols, int rows, int width, int height) {
		
		_animals = new ArrayList<>();
		_region_mngr = new RegionManager(cols, rows, width, height);
		_time = 0.0;
		
		notify_on_reset();
	}

	@Override
	public void add_observer(EcoSysObserver o) {

		if (!_observers.contains(o))
			_observers.add(o);
		
		o.on_register(_time, _region_mngr, new ArrayList<>(_animals));
	}

	@Override
	public void remove_observer(EcoSysObserver o) {
		_observers.remove(o);
	}
	
	private void notify_on_advanced(double dt) {
		for (EcoSysObserver o : _observers)
			o.on_advanced(_time, _region_mngr, new ArrayList<>(_animals), dt);
	}
	
	private void notify_on_reset() {
		for (EcoSysObserver o : _observers)
			o.on_reset(_time, _region_mngr, new ArrayList<>(_animals));
	}
	
	private void notify_on_animal_added(Animal a) {
		for (EcoSysObserver o : _observers)
			o.on_animal_added(_time, _region_mngr, new ArrayList<>(_animals), a);
	}
	
	private void notify_on_region_set(int row, int col, Region r) {
		for (EcoSysObserver o : _observers)
			o.on_region_set(row, col, _region_mngr, r);
	}

}
