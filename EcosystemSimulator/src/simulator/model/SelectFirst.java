package simulator.model;

import java.util.List;

public class SelectFirst implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {

		Animal first = null;

		if (!as.isEmpty())
			first = as.get(0);

		return first;
	}

}
