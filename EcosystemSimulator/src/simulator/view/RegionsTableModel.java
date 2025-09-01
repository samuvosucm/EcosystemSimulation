package simulator.view;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.MapInfo.RegionData;
import simulator.model.RegionInfo;

@SuppressWarnings("serial")
public class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {

	private Controller _ctrl;
	private Map<RegionData, Map<Diet, Integer>> _map;
	private List<RegionData> _regions;
	private List<String> _headers;
	
	public RegionsTableModel(Controller ctrl) {
		
		_map = new HashMap<>();
		_regions = new ArrayList<>();
		_ctrl = ctrl;
		createHeaders();
		
		_ctrl.add_observer(this);
	}
	
	private void createHeaders() {
		_headers = new ArrayList<>();
		_headers.add("Row");
		_headers.add("Col");
		_headers.add("Desc.");
		for (Diet d : Diet.values())
			_headers.add(d.toString());
	}
	
	@Override
	public int getRowCount() {
		return _map.size();
	}

	@Override
	public int getColumnCount() {
		return _headers.size();
	}
	
	@Override
	public String getColumnName(int column) {
		
		return _headers.get(column);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		Object s = null;
		RegionData region = _regions.get(rowIndex);
		
		switch (columnIndex) {
		case 0: 
			s = region.row();
			break;
		case 1:
			s = region.col();
			break;
		case 2:
			s = region.r().toString();
			break;
		default:
			s = _map.get(region).get(Diet.values()[columnIndex - 3]);
			break;
		}
		
		return s;
	}

	@Override
	public void on_register(double time, MapInfo map, List<AnimalInfo> animals) {	
		set_data(map);
		set_regions(map);
		fireTableDataChanged();
	}

	@Override
	public void on_reset(double time, MapInfo map, List<AnimalInfo> animals) {
		_map.clear();
		_regions.clear();
		set_data(map);
		set_regions(map);
		fireTableDataChanged();
	}
	
	private void set_regions(MapInfo map) {
		
		Iterator<RegionData> it = map.iterator();
		while (it.hasNext()) {
			RegionData r = it.next();
			_regions.add(r);
		}
	}
	
	private void set_data(MapInfo map) {
		
		Iterator<RegionData> it = map.iterator();
		while (it.hasNext()) {
			RegionData r = it.next();
			_map.put(r, new HashMap<>());
			
			Map<Diet, Integer> dietMap = _map.get(r);	
			for (Diet diet : Diet.values())
				dietMap.put(diet, (int) r.r().get_animals_info().stream().filter(animal -> animal.get_diet() == diet).count());
		}
	}

	@Override
	public void on_animal_added(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		
		int col = (int) a.get_position().getX() / map.get_region_width();
		int row = (int) a.get_position().getY() / map.get_region_height();
		int index = row * map.get_cols() + col;
		Map<Diet, Integer> dietMap = _map.get(_regions.get(index));
		dietMap.put(a.get_diet(), dietMap.get(a.get_diet()) + 1);	
	}

	@Override
	public void on_region_set(int row, int col, MapInfo map, RegionInfo r) {
		
		int index = row * map.get_cols() + col;
		Map<Diet, Integer> dietMap = _map.get(_regions.get(index));
		_map.remove(_regions.get(index));
		_regions.set(index, new RegionData(row, col, r));
		_map.put(_regions.get(index), dietMap);
		fireTableDataChanged();
	}

	@Override
	public void on_advanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		
		_map.clear();
		set_data(map);
		fireTableDataChanged();
	}

}
