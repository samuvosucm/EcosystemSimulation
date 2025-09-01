package simulator.model;

import java.util.List;

import simulator.misc.Vector2D;

public class SelectClosest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {

		Animal closest = null;

		if (!as.isEmpty()) {

			Vector2D a_pos = a.get_position();
			closest = as.get(0);

			for (int i = 1; i < as.size(); i++) {

				Animal animal = as.get(i);
				if (closest.get_position().distanceTo(a_pos) > animal.get_position().distanceTo(a_pos))
					closest = animal;
			}
		}

		return closest;
	}

}
