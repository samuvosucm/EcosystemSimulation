package simulator.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

@SuppressWarnings("serial")
public class MapWindow extends JFrame implements EcoSysObserver {

	private Controller _ctrl;
	private AbstractMapViewer _viewer;
	private Frame _parent;

	MapWindow(Frame parent, Controller ctrl) {
		super("[MAP VIEWER]");
		_ctrl = ctrl;
		_parent = parent;
		intiGUI();
		_ctrl.add_observer(this);
	}

	private void intiGUI() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);
		
		_viewer = new MapViewer();
		add(_viewer, BorderLayout.CENTER);
		
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
		        _ctrl.remove_observer(MapWindow.this);
			}
		});
		pack();
		if (_parent != null)
			setLocation(_parent.getLocation().x + _parent.getWidth() / 2 - getWidth() / 2,
					_parent.getLocation().y + _parent.getHeight() / 2 - getHeight() / 2);
		setResizable(false);
		setVisible(true);
	}

	@Override
	public void on_register(double time, MapInfo map, List<AnimalInfo> animals) {
		SwingUtilities.invokeLater(() -> { _viewer.reset(time, map, animals); pack(); });
	}

	@Override
	public void on_reset(double time, MapInfo map, List<AnimalInfo> animals) {
		SwingUtilities.invokeLater(() -> { _viewer.reset(time, map, animals); pack(); });
	}

	@Override
	public void on_animal_added(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) { }

	@Override
	public void on_region_set(int row, int col, MapInfo map, RegionInfo r) { }

	@Override
	public void on_advanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		SwingUtilities.invokeLater(() -> { _viewer.update(animals, time); });
	}

}
