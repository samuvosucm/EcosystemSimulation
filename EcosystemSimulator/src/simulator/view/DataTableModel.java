package simulator.view;

import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.State;

import java.util.HashMap;

@SuppressWarnings("serial")
public class DataTableModel extends AbstractTableModel implements EcoSysObserver{
	
	public record Data(int hd, int ch) {};

	private InfoTable _table;
	private int steps;
	private Map<Integer, Data> _map;
	private String[] _headers = { "Steps", "Hervibores in Danger", "Carnivores in Hunger" };
	
	public DataTableModel(Controller ctrl, InfoTable table) {
		steps = 0;
		_table = table;
		_map = new HashMap<>();
		ctrl.add_observer(this);
	}
	
	@Override
	public int getRowCount() {
		return _map.size();
	}

	@Override
	public String getColumnName(int column) {
		return _headers[column];
	}
	
	@Override
	public int getColumnCount() {
		return _headers.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		Object o = null;
		switch(columnIndex) {
		case 0:
			o = rowIndex;
			break;
		case 1:
			o = _map.get(rowIndex).hd();
			break;
		case 2:
			o = _map.get(rowIndex).ch();
			break;
		}
		return o;
	}

	@Override
	public void on_register(double time, MapInfo map, List<AnimalInfo> animals) {
		
		Data data = new Data((int) animals.stream().filter(a -> a.get_diet() == Diet.HERBIVORE && a.get_state() == State.DANGER).count(), 
				(int) animals.stream().filter(a -> a.get_diet() == Diet.CARNIVORE && a.get_state() == State.HUNGER).count());
		_map.put(steps, data);
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
		
		steps++;
		Data data = new Data((int) _table._tableModel.getValueAt(0, 4), (int) _table._tableModel.getValueAt(1, 3));
		_map.put(steps, data);
		fireTableDataChanged();
	}

}
