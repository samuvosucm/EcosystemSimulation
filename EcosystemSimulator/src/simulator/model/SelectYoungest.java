package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {

		Animal youngest = null;

		if (!as.isEmpty()) {

			youngest = as.get(0);

			for (int i = 1; i < as.size(); i++)
				youngest = youngest.get_age() > as.get(i).get_age() ? as.get(i) : youngest;

		}

		return youngest;
	}

}
