package simulator.model;

import java.util.List;

public interface EcoSysObserver {
	
	void on_register(double time, MapInfo map, List<AnimalInfo> animals);

	void on_reset(double time, MapInfo map, List<AnimalInfo> animals);

	void on_animal_added(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a);

	void on_region_set(int row, int col, MapInfo map, RegionInfo r);

	void on_advanced(double time, MapInfo map, List<AnimalInfo> animals, double dt);
}
