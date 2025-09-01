package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import simulator.control.Controller;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private Controller _ctrl;

	public MainWindow(Controller ctrl) {
		super("[ECOSYSTEM SIMULATOR]");
		_ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);
		
		ControlPanel controlPanel = new ControlPanel(_ctrl);
		mainPanel.add(controlPanel, BorderLayout.PAGE_START);
		
		StatusBar statusBar = new StatusBar(_ctrl);
		mainPanel.add(statusBar, BorderLayout.PAGE_END);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		mainPanel.add(contentPanel, BorderLayout.CENTER);
		
		InfoTable speciesPanel = new InfoTable("Species", new SpeciesTableModel(_ctrl));
		contentPanel.add(speciesPanel);
		speciesPanel.setPreferredSize(new Dimension(500, 250));
		
		InfoTable regionsPanel = new InfoTable("Regions", new RegionsTableModel(_ctrl));
		contentPanel.add(regionsPanel);
		regionsPanel.setPreferredSize(new Dimension(500, 250));
		
		InfoTable dataPanel = new InfoTable("Data", new DataTableModel(_ctrl, speciesPanel));
		contentPanel.add(dataPanel);
		dataPanel.setPreferredSize(new Dimension(500, 250));

		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
		        ViewUtils.quit(MainWindow.this);
			}
		});
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		setVisible(true);
		}	
}
