package simulator.model;

public class DefaultRegion extends Region {

	@Override
	public void update(double dt) {
	}

	@Override
	public double get_food(Animal a, double dt) {

		double food = 0.0;

		if (a.get_diet().equals(Diet.HERBIVORE)) {

			int n = (int) _animals.stream().filter(animal -> animal.get_diet() == Diet.HERBIVORE).count();
			food = 60.0 * Math.exp(-Math.max(0, n - 5.0) * 2.0) * dt;
		}

		return food;
	}
	
	public String toString() {
		return "default";
	}

}
