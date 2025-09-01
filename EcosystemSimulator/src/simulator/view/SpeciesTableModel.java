package simulator.view;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.State;

@SuppressWarnings("serial")
public class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {

	private Controller _ctrl;
	private Map<String, Map<State, Integer>> _map;
	private List<String> _headers;

	SpeciesTableModel(Controller ctrl) {

		_map = new HashMap<>();
		_ctrl = ctrl;
		createHeaders();

		_ctrl.add_observer(this);
	}

	private void createHeaders() {
		_headers = new ArrayList<>();
		_headers.add("Species");
		for (State s : State.values())
			_headers.add(s.toString());
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

		String key = new ArrayList<>(_map.keySet()).get(rowIndex);

		if (columnIndex == 0)
			return key;
		else
			return _map.get(key).get(State.values()[columnIndex - 1]);
	}

	@Override
	public void on_register(double time, MapInfo map, List<AnimalInfo> animals) {

		set_data(animals);
		fireTableDataChanged();
	}

	private void set_data(List<AnimalInfo> animals) {

		Iterator<AnimalInfo> it = animals.iterator();
		while (it.hasNext()) {

			AnimalInfo a = it.next();
			add_animal(a);
		}
	}

	private void add_animal(AnimalInfo a) {

		if (!_map.containsKey(a.get_genetic_code())) {

			_map.put(a.get_genetic_code(), new HashMap<>());
			Map<State, Integer> state_map = _map.get(a.get_genetic_code());
			for (State state : State.values())
				state_map.put(state, 0);
		}

		Map<State, Integer> state_map = _map.get(a.get_genetic_code());
		state_map.put(a.get_state(), state_map.get(a.get_state()) + 1);
	}

	@Override
	public void on_reset(double time, MapInfo map, List<AnimalInfo> animals) {

		_map.clear();
		fireTableDataChanged();
	}

	@Override
	public void on_animal_added(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {

		add_animal(a);
		fireTableDataChanged();
	}

	@Override
	public void on_region_set(int row, int col, MapInfo map, RegionInfo r) {
	}

	@Override
	public void on_advanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		_map.clear();
		set_data(animals);
		fireTableDataChanged();
	}

}
