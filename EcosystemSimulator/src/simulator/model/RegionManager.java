package simulator.model;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONArray;

public class RegionManager implements AnimalMapView {

	private int _cols;
	private int _rows;
	private int _width;
	private int _height;
	private int _region_width;
	private int _region_height;

	private Region[][] _regions;
	private Map<Animal, Region> _animal_region;

	public RegionManager(int cols, int rows, int width, int height) {

		_cols = cols;
		_rows = rows;
		_width = width;
		_height = height;
		_region_width = width / cols;
		_region_height = height / rows;
		_animal_region = new HashMap<>();
		_regions = new Region[rows][cols];
		init_regions();
	}

	private void init_regions() {

		for (int i = 0; i < _rows; i++)
			for (int j = 0; j < _cols; j++) {
				_regions[i][j] = new DefaultRegion();
			}
	}

	@Override
	public int get_cols() {
		return _cols;
	}

	@Override
	public int get_rows() {
		return _rows;
	}

	@Override
	public int get_width() {
		return _width;
	}

	@Override
	public int get_height() {
		return _height;
	}

	@Override
	public int get_region_width() {
		return _region_width;
	}

	@Override
	public int get_region_height() {
		return _region_height;
	}

	@Override
	public double get_food(Animal a, double dt) {
		return _animal_region.get(a).get_food(a, dt);
	}

	@Override
	public List<Animal> get_animals_in_range(Animal a, Predicate<Animal> filter) {

		List<Animal> animals_in_range = new ArrayList<>();

		int min_col = Math.max(0, (int) (a.get_position().getX() - a.get_sight_range()) / get_region_width());
		int max_col = Math.min(get_cols() - 1,
				(int) (a.get_position().getX() + a.get_sight_range()) / get_region_width());
		int min_row = Math.max(0, (int) (a.get_position().getY() - a.get_sight_range()) / get_region_height());
		int max_row = Math.min(get_rows() - 1,
				(int) (a.get_position().getY() + a.get_sight_range()) / get_region_height());

		for (int i = min_row; i <= max_row; i++)
			for (int j = min_col; j <= max_col; j++) {

				for (Animal animal : _regions[i][j].get_animals()) {

					if (animal.get_position().distanceTo(a.get_position()) < a.get_sight_range() && filter.test(animal))
						animals_in_range.add(animal);
				}
			}

		return animals_in_range;
	}

	void set_region(int row, int col, Region r) {

		for (Animal a : _regions[row][col].get_animals()) {

			r.add_animal(a);
			_animal_region.put(a, r);
		}

		_regions[row][col] = r;
	}

	void register_animal(Animal a) {

		a.init(this);

		int col = (int) a.get_position().getX() / get_region_width();
		int row = (int) a.get_position().getY() / get_region_height();

		_regions[row][col].add_animal(a);
		_animal_region.put(a, _regions[row][col]);
	}

	void unregister_animal(Animal a) {

		_animal_region.get(a).remove_animal(a);
		_animal_region.remove(a);
	}

	void update_animal_region(Animal a) {

		int col = (int) a.get_position().getX() / get_region_width();
		int row = (int) a.get_position().getY() / get_region_height();

		if (_regions[row][col] != _animal_region.get(a)) {

			_regions[row][col].add_animal(a);
			_animal_region.get(a).remove_animal(a);
			_animal_region.remove(a, _animal_region.get(a));
			_animal_region.put(a, _regions[row][col]);
		}
	}

	void update_all_regions(double dt) {

		for (int i = 0; i < _rows; i++)
			for (int j = 0; j < _cols; j++) {
				_regions[i][j].update(dt);
			}
	}

	public JSONObject as_JSON() {

		JSONArray regions_array = new JSONArray();

		for (int i = 0; i < _rows; i++)
			for (int j = 0; j < _cols; j++) {

				JSONObject region = new JSONObject();
				region.put("row", i);
				region.put("col", j);
				region.put("data", _regions[i][j].as_JSON());
				regions_array.put(region);
			}

		return new JSONObject().put("regiones", regions_array);
	}

	@Override
	public Iterator<RegionData> iterator() {
			
		Iterator<RegionData> it = new Iterator<>() {
			
			private int row = 0;
			private int col = -1;
			@Override
			public boolean hasNext() {
				return row + 1 < get_rows() || col + 1 < get_cols();
			}

			@Override
			public RegionData next() {
				
				col++;
				if (col == get_cols()) {
					row++;
					col = 0;
				}
				
				return new RegionData(row, col, _regions[row][col]);
			}
			
			
		};
		
		return it;
	
	}

}
