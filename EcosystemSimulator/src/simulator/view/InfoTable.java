package simulator.view;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class InfoTable extends JPanel {

	String _title;
	TableModel _tableModel;

	InfoTable(String title, TableModel tableModel) {
		_title = title;
		_tableModel = tableModel;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(_title));	
		
		JTable _table = new JTable(_tableModel);
		add(new JScrollPane(_table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
	}
}
