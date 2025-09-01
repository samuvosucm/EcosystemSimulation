package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.MapInfo.RegionData;

public class CarViewer implements EcoSysObserver {

	Map<RegionData, Integer> _map;
	private List<RegionData> _regions;

	
	public CarViewer(Controller ctrl) {
		
		_map = new HashMap<>();
		_regions = new ArrayList<>();
		ctrl.add_observer(this);
	}
	
	@Override
	public void on_register(double time, MapInfo map, List<AnimalInfo> animals) {	
		
		Iterator<RegionData> it = map.iterator();
		while (it.hasNext()) {
			RegionData r = it.next();
			_regions.add(r);
		}
	}

	@Override
	public void on_reset(double time, MapInfo map, List<AnimalInfo> animals) {		
	}

	@Override
	public void on_animal_added(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {		
	}

	@Override
	public void on_region_set(int row, int col, MapInfo map, RegionInfo r) {		
	}

	@Override
	public void on_advanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		
		for (RegionData r : map) {
			
			int n  = (int) r.r().get_animals_info().stream().filter(a -> a.get_diet() == Diet.CARNIVORE).count();
			
			if (n > 3) {
				int ncar = _map.getOrDefault(r, 0) + 1;
				_map.put(r, ncar);
			}
		}
	}
	
	public void imprimir() {
		for (RegionData r : _regions) {
			System.out.println("Region " + r.row() + ", " + r.col() + ": " + (!_map.containsKey(r) ? 0 : _map.get(r)));
		}
	}

}
