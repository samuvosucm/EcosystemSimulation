package simulator.model;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region {

	public static final double DEFAULT_FOOD = 1000.0;
	public static final double DEFAULT_FACTOR = 2.0;

	private double _food;
	private double _factor;

	public DynamicSupplyRegion(double food, double factor) {

		_food = food;
		_factor = factor;
	}

	@Override
	public void update(double dt) {

		if (Utils._rand.nextDouble() < 0.5)
			_food += dt * _factor;
	}

	@Override
	public double get_food(Animal a, double dt) {

		double food = 0.0;

		if (a.get_diet().equals(Diet.HERBIVORE)) {

			int n = (int) _animals.stream().filter(animal -> animal.get_diet() == Diet.HERBIVORE).count();
			food = Math.min(_food, 60.0 * Math.exp(-Math.max(0, n - 5.0) * 2.0) * dt);
		}
		_food -= food;

		return food;
	}
	
	public String toString() {
		return "dynamic";
	}

}
