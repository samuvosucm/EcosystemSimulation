package simulator.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

@SuppressWarnings("serial")
public class StatusBar extends JPanel implements EcoSysObserver {

	private JLabel _timeLabel;
	private JLabel _animalsLabel;
	private JLabel _dimensionLabel;

	StatusBar(Controller ctrl) {
		initGUI();
		ctrl.add_observer(this);
	}

	private void initGUI() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setBorder(BorderFactory.createBevelBorder(1));

		_timeLabel = new JLabel();
		_timeLabel.setMaximumSize(new Dimension(80, 20));
		_timeLabel.setMinimumSize(new Dimension(80, 20));
		_timeLabel.setPreferredSize(new Dimension(80, 20));
		this.add(_timeLabel);
		add_separator();

		_animalsLabel = new JLabel();
		_animalsLabel.setMaximumSize(new Dimension(130, 20));
		_animalsLabel.setMinimumSize(new Dimension(130, 20));
		_animalsLabel.setPreferredSize(new Dimension(130, 20));
		this.add(_animalsLabel);
		add_separator();

		_dimensionLabel = new JLabel();
		_dimensionLabel.setMaximumSize(new Dimension(150, 20));
		_dimensionLabel.setMinimumSize(new Dimension(150, 20));
		_dimensionLabel.setPreferredSize(new Dimension(150, 20));
		this.add(_dimensionLabel);

	}

	private void add_separator() {
		
		JSeparator s = new JSeparator(JSeparator.VERTICAL);
		s.setPreferredSize(new Dimension(10, 20));
		this.add(s);
	}

	private void set_data(double time, MapInfo map, List<AnimalInfo> animals) {
		set_time(time);
		set_animals(animals.size());
		set_dimension(map.get_width(), map.get_height(), map.get_rows(), map.get_cols());
	}
	
	@Override
	public void on_register(double time, MapInfo map, List<AnimalInfo> animals) {
		set_data(time, map, animals);
	}

	@Override
	public void on_reset(double time, MapInfo map, List<AnimalInfo> animals) {
		set_data(time, map, animals);
	}

	@Override
	public void on_animal_added(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		set_animals(animals.size());
	}

	@Override
	public void on_region_set(int row, int col, MapInfo map, RegionInfo r) {}

	@Override
	public void on_advanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		set_time(time + dt);
		set_animals(animals.size());
	}

	private void set_time(double time) {
		_timeLabel.setText("Time: " + String.format("%.3f", time));
	}

	private void set_animals(int animals) {
		_animalsLabel.setText("Total Animals: " + animals);
	}

	private void set_dimension(int width, int height, int rows, int cols) {
		_dimensionLabel.setText("Dimension: " + width + "x" + height + " " + cols + "x" + rows);
	}

}
