package simulator.view;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.table.DefaultTableModel;

import org.json.JSONObject;
import org.json.JSONArray;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class ChangeRegionsDialog extends JDialog implements EcoSysObserver {

	private DefaultComboBoxModel<String> _regionsModel;
	private DefaultComboBoxModel<Integer> _fromRowModel;
	private DefaultComboBoxModel<Integer> _toRowModel;
	private DefaultComboBoxModel<Integer> _fromColModel;
	private DefaultComboBoxModel<Integer> _toColModel;

	private DefaultTableModel _dataTableModel;
	private Controller _ctrl;
	private List<JSONObject> _regionsInfo;

	private String[] _headers = { "Key", "Value", "Description" };

	private static final String HELP_LABEL = "Select a region type, the rows/cols interval, and provide"
			+ " values for the parameters in the Value column (default values are used for parameters with no values";

	JComboBox<String> _regionComboBox;
	JComboBox<Integer> _fromRowCombobox;
	JComboBox<Integer> _toRowCombobox;
	JComboBox<Integer> _fromColCombobox;
	JComboBox<Integer> _toColCombobox;

	ChangeRegionsDialog(Controller ctrl) {
		super((Frame) null, true);
		_ctrl = ctrl;
		initGUI();
		_ctrl.add_observer(this);
	}

	private void initGUI() {

		setTitle("Change Regions");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		setContentPane(mainPanel);

		// TODO crea varios paneles para organizar los componentes visuales en el
		// dialogo, y añadelos al mainpanel. P.ej., uno para el texto de ayuda,
		// uno para la tabla, uno para los combobox, y uno para los botones.

		JPanel helpPanel = new JPanel();
		helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
		mainPanel.add(helpPanel);

		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
		mainPanel.add(tablePanel);

		JPanel comboboxPanel = new JPanel();
		mainPanel.add(comboboxPanel);

		JPanel buttonPanel = new JPanel();
		mainPanel.add(buttonPanel);

		// TODO crear el texto de ayuda que aparece en la parte superior del diálogo y
		// añadirlo al panel correspondiente diálogo (Ver el apartado Figuras)

		JLabel helpLabel = new JLabel("<html><p>" + HELP_LABEL + "</p></html>");
		helpLabel.setAlignmentX(CENTER_ALIGNMENT);
		helpPanel.add(helpLabel);

		// _regionsInfo se usará para establecer la información en la tabla
		_regionsInfo = Main._regions_factory.get_info();
		// _dataTableModel es un modelo de tabla que incluye todos los parámetros de
		// la region
		_dataTableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 1;
			}
		};
		_dataTableModel.setColumnIdentifiers(_headers);

		// TODO crear un JTable que use _dataTableModel, y añadirlo al diálogo
		JTable table = new JTable(_dataTableModel);
		JScrollPane tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablePanel.add(tableScrollPane);

		// _regionsModel es un modelo de combobox que incluye los tipos de regiones
		_regionsModel = new DefaultComboBoxModel<>();

		// TODO añadir la descripción de todas las regiones a _regionsModel, para eso
		// usa la clave “desc” o “type” de los JSONObject en _regionsInfo,
		// ya que estos nos dan información sobre lo que puede crear la factoría.
		for (JSONObject jo : _regionsInfo)
			_regionsModel.addElement(jo.getString("type"));

		// TODO crear un combobox que use _regionsModel y añadirlo al diálogo.
		_regionComboBox = new JComboBox<>(_regionsModel);
		_regionComboBox.addActionListener(e -> addEvent());
		comboboxPanel.add(new JLabel("Region type: "));
		comboboxPanel.add(_regionComboBox);

		// TODO crear 4 modelos de combobox para _fromRowModel, _toRowModel,
		// _fromColModel y _toColModel.
		_fromRowModel = new DefaultComboBoxModel<>();
		_toRowModel = new DefaultComboBoxModel<>();

		_fromColModel = new DefaultComboBoxModel<>();
		_toColModel = new DefaultComboBoxModel<>();

		// TODO crear 4 combobox que usen estos modelos y añadirlos al diálogo.
		_fromRowCombobox = new JComboBox<>(_fromRowModel);
		_toRowCombobox = new JComboBox<>(_toRowModel);

		comboboxPanel.add(new JLabel("Row from/to: "));
		comboboxPanel.add(_fromRowCombobox);
		comboboxPanel.add(_toRowCombobox);

		_fromColCombobox = new JComboBox<>(_fromColModel);
		_toColCombobox = new JComboBox<>(_toColModel);
		comboboxPanel.add(new JLabel("Column from/to: "));
		comboboxPanel.add(_fromColCombobox);
		comboboxPanel.add(_toColCombobox);

		// TODO crear los botones OK y Cancel y añadirlos al diálogo.
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> setVisible(false));
		buttonPanel.add(cancelButton);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(e -> okButton());
		buttonPanel.add(okButton);

		setPreferredSize(new Dimension(700, 400)); // puedes usar otro tamaño
		pack();
		setResizable(false);
		setVisible(false);
	}

	public void open(Frame parent) {
		setLocation(//
				parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, //
				parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
		pack();
		setVisible(true);
	}

	private void addEvent() {

		JSONObject info = _regionsInfo.get(_regionComboBox.getSelectedIndex());
		JSONObject data = info.getJSONObject("data");

		_dataTableModel.setRowCount(0);
		for (String key : data.keySet()) {
			String[] v = { key, null, data.getString(key) };
			_dataTableModel.addRow(v);
		}
	}

	private void okButton() {

		try {
			JSONObject regions_value = new JSONObject();

			JSONArray row = new JSONArray();
			row.put(_fromRowCombobox.getSelectedItem());
			row.put(_toRowCombobox.getSelectedItem());

			if (row.getInt(0) > row.getInt(1))
				throw new Exception("Invalid row range");

			regions_value.put("row", row);

			JSONArray col = new JSONArray();
			col.put(_fromColCombobox.getSelectedItem());
			col.put(_toColCombobox.getSelectedItem());

			if (col.getInt(0) > col.getInt(1))
				throw new Exception("Invalid column range");

			regions_value.put("col", col);

			JSONObject spec = new JSONObject();
			spec.put("type", _regionComboBox.getSelectedItem());

			JSONObject data = new JSONObject();
			for (int i = 0; i < _dataTableModel.getRowCount(); i++)
				if (_dataTableModel.getValueAt(i, 1) != null) {
					data.put((String) _dataTableModel.getValueAt(i, 0), _dataTableModel.getValueAt(i, 1));
				}

			spec.put("data", data);
			regions_value.put("spec", spec);
			_ctrl.set_regions(regions_value);
			setVisible(false);
		} catch (Exception e) {
			ViewUtils.showErrorMsg(e.getMessage());
		}
	}

	private void set_data(MapInfo map) {

		for (int i = 0; i < map.get_rows(); i++) {
			_fromRowModel.addElement(i);
			_toRowModel.addElement(i);
		}

		for (int i = 0; i < map.get_cols(); i++) {
			_fromColModel.addElement(i);
			_toColModel.addElement(i);
		}
	}

	@Override
	public void on_register(double time, MapInfo map, List<AnimalInfo> animals) {
		set_data(map);
	}

	@Override
	public void on_reset(double time, MapInfo map, List<AnimalInfo> animals) {

		_fromRowModel.removeAllElements();
		_toRowModel.removeAllElements();
		_fromColModel.removeAllElements();
		_toColModel.removeAllElements();
		set_data(map);
	}

	@Override
	public void on_animal_added(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
	}

	@Override
	public void on_region_set(int row, int col, MapInfo map, RegionInfo r) {
	}

	@Override
	public void on_advanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
	}

}
