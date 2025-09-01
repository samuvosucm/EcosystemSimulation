package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {

	private static final double INIT_SIGHT_RANGE = 50.0;
	private static final double INIT_SPEED = 60.0;
	private static final double DEAD_AGE = 14.0;
	private static final double NORMAL_ENERGY_TO_HUNGER = 50.0;
	private static final double NORMAL_DESIRE_TO_MATE = 65.0;
	private static final double NORMAL_MOVE = 0.007;
	private static final double NORMAL_ENERGY = 18.0;
	private static final double NORMAL_DESIRE = 30.0;
	private static final double HUNGER_MOVE_1 = 3.0;
	private static final double HUNGER_MOVE_2 = 0.007;
	private static final double HUNGER_ENERGY_1 = 18.0;
	private static final double HUNGER_ENERGY_2 = 1.2;
	private static final double HUNGER_DESIRE = 30.0;
	private static final double HUNGER_EAT_ENERGY = 50.0;
	private static final double HUNGER_ENERGY_STATE_CHANGE = 50.0;
	private static final double HUNGER_DESIRE_TO_NORMAL = 65.0;
	private static final double MATE_MOVE_1 = 3.0;
	private static final double MATE_MOVE_2 = 0.007;
	private static final double MATE_ENERGY_1 = 18.0;
	private static final double MATE_ENERGY_2 = 1.2;
	private static final double MATE_DESIRE = 30.0;
	private static final double BABY_ENERGY = 10.0;
	private static final double MATE_DESIRE_TO_NORMAL = 65.0;
	private static final double MATE_ENERGY_TO_HUNGER = 50.0;

	private Animal _hunt_target;
	private SelectionStrategy _hunting_strategy;

	public Wolf(SelectionStrategy mate_strategy, SelectionStrategy hunting_strategy, Vector2D pos) {
		super("Wolf", Diet.CARNIVORE, INIT_SIGHT_RANGE, INIT_SPEED, mate_strategy, pos);
		_hunting_strategy = hunting_strategy;
	}

	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		_hunting_strategy = p1.get_hunting_strategy();
		_hunt_target = null;
	}

	@Override
	public void update(double dt) {

		switch (_state) {
		case DEAD:
			return;
		case HUNGER:
			hunger_state(dt);
		case NORMAL:
			normal_state(dt);
			break;
		case DANGER:
			break;
		case MATE:
			mate_state(dt);
			break;
		}

		if (!_pos.within_bounds(_region_mngr.get_width(), _region_mngr.get_height())) {

			_pos.adjust(_region_mngr.get_width(), _region_mngr.get_height());
			state_to_normal();
		}

		if (_energy == MIN_RATE || _age > DEAD_AGE)
			_state = State.DEAD;

		if (!_state.equals(State.DEAD)) {

			double energy = _energy + _region_mngr.get_food(this, dt);
			_energy = Utils.constrain_value_in_range(energy, MIN_RATE, MAX_RATE);
		}
	}

	private void normal_state(double dt) {

		// logic
		normal_state_logic(dt);

		// state change
		if (_energy < NORMAL_ENERGY_TO_HUNGER)
			state_to_hunger();
		else if (_desire > NORMAL_DESIRE_TO_MATE)
			state_to_mate();

	}

	private void normal_state_logic(double dt) {

		if (_dest.distanceTo(_pos) < CLOSING_GAP_RANGE) {

			double x = Utils._rand.nextDouble(_region_mngr.get_width());
			double y = Utils._rand.nextDouble(_region_mngr.get_height());
			_dest = new Vector2D(x, y);
		}

		super.move(_speed * dt * Math.exp((_energy - MAX_RATE) * NORMAL_MOVE));
		_age += dt;

		double energy = _energy - NORMAL_ENERGY * dt;
		_energy = Utils.constrain_value_in_range(energy, MIN_RATE, MAX_RATE);

		double desire = _desire + NORMAL_DESIRE * dt;
		_desire = Utils.constrain_value_in_range(desire, MIN_RATE, MAX_RATE);
	}

	private void hunger_state(double dt) {

		// logic
		if (_hunt_target == null || _hunt_target.get_state().equals(State.DEAD)
				|| _hunt_target.get_position().distanceTo(_pos) > _sight_range) {

			_hunt_target = _hunting_strategy.select(this,
					_region_mngr.get_animals_in_range(this, animal -> animal.get_diet().equals(Diet.HERBIVORE)));
		}

		if (_hunt_target == null)
			normal_state_logic(dt);
		else {

			_dest = _hunt_target.get_position();
			super.move(HUNGER_MOVE_1 * _speed * dt * Math.exp((_energy - MAX_RATE) * HUNGER_MOVE_2));
			_age += dt;

			double energy = _energy - HUNGER_ENERGY_1 * HUNGER_ENERGY_2 * dt;
			_energy = Utils.constrain_value_in_range(energy, MIN_RATE, MAX_RATE);

			double desire = _desire + HUNGER_DESIRE;
			_desire = Utils.constrain_value_in_range(desire, MIN_RATE, MAX_RATE);

			if (_pos.distanceTo(_hunt_target.get_position()) < CLOSING_GAP_RANGE) {

				_hunt_target.set_state(State.DEAD);
				_hunt_target = null;
				_energy = Utils.constrain_value_in_range(_energy + HUNGER_EAT_ENERGY, MIN_RATE, MAX_RATE);
			}
		}

		// state change
		if (_energy > HUNGER_ENERGY_STATE_CHANGE) {

			if (_desire < HUNGER_DESIRE_TO_NORMAL)
				state_to_normal();
			else
				state_to_mate();
		}
	}

	private void mate_state(double dt) {

		// logic
		if (_mate_target != null) {
			if (_mate_target.get_state().equals(State.DEAD)
					|| _mate_target.get_position().distanceTo(_pos) > _sight_range)
				_mate_target = null;
		}

		if (_mate_target == null) {

			_mate_target = _mate_strategy.select(this,
					_region_mngr.get_animals_in_range(this, animal -> animal.get_genetic_code().equals("Wolf")));

			if (_mate_target == null)
				normal_state_logic(dt);
		}

		if (_mate_target != null) {

			_dest = _mate_target.get_position();
			super.move(MATE_MOVE_1 * _speed * dt * Math.exp((_energy - MAX_RATE) * MATE_MOVE_2));
			_age += dt;

			double energy = _energy - MATE_ENERGY_1 * MATE_ENERGY_2 * dt;
			_energy = Utils.constrain_value_in_range(energy, MIN_RATE, MAX_RATE);

			double desire = _desire + MATE_DESIRE;
			_desire = Utils.constrain_value_in_range(desire, MIN_RATE, MAX_RATE);

			if (_pos.distanceTo(_mate_target.get_position()) < CLOSING_GAP_RANGE) {

				_desire = MIN_RATE;
				_mate_target.set_desire(MIN_RATE);

				if (_baby == null && Utils._rand.nextDouble() < BABY_PROBABILITY)
					_baby = new Wolf(this, _mate_target);

				_energy = Utils.constrain_value_in_range(_energy - BABY_ENERGY, MIN_RATE, MAX_RATE);
				_mate_target = null;
			}
		}

		// state change
		if (_energy < MATE_ENERGY_TO_HUNGER)
			state_to_hunger();
		else if (_desire < MATE_DESIRE_TO_NORMAL)
			state_to_normal();
	}

	public SelectionStrategy get_hunting_strategy() {
		return _hunting_strategy;
	}

	private void state_to_normal() {

		_state = State.NORMAL;
		_hunt_target = null;
		_mate_target = null;
	}

	private void state_to_mate() {

		_state = State.MATE;
		_hunt_target = null;
	}

	private void state_to_hunger() {

		_state = State.HUNGER;
		_mate_target = null;
	}

}