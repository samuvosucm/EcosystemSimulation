package simulator.view;

import java.util.List;
import java.util.Map;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import java.util.HashMap;

public class NoViewer implements EcoSysObserver {

	private Map<AnimalInfo, Double> _map;
	private int numero;
	
	public NoViewer(Controller ctrl) {
		
		_map = new HashMap<>();
		numero = 0;
		
		ctrl.add_observer(this);
	}
	@Override
	public void on_register(double time, MapInfo map, List<AnimalInfo> animals) {
		
		for (AnimalInfo a : animals)
			_map.put(a, a.get_position().getY());
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

		numero = 0;
		for (AnimalInfo a : animals) {
			
			if (_map.containsKey(a)) {
				if (a.get_position().getY() < _map.get(a))
					numero++;
			}
		}
		
		for (AnimalInfo a : animals)
			_map.put(a, a.get_position().getY());
		
		System.out.println("Animales: " + numero);

	}


}
