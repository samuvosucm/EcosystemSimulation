package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {

	private static final double INIT_SIGHT_RANGE = 40.0;
	private static final double INIT_SPEED = 35.0;
	private static final double DEAD_AGE = 8.0;
	private static final double NORMAL_DESIRE_TO_MATE = 65.0;
	private static final double NORMAL_MOVE = 0.007;
	private static final double NORMAL_ENERGY = 20.0;
	private static final double NORMAL_DESIRE = 40.0;
	private static final double DANGER_MOVE_1 = 2.0;
	private static final double DANGER_MOVE_2 = 0.007;
	private static final double DANGER_ENERGY_1 = 20.0;
	private static final double DANGER_ENERGY_2 = 1.2;
	private static final double DANGER_DESIRE = 40.0;
	private static final double DANGER_DESIRE_TO_NORMAL = 65.0;
	private static final double MATE_MOVE_1 = 2.0;
	private static final double MATE_MOVE_2 = 0.007;
	private static final double MATE_ENERGY_1 = 20.0;
	private static final double MATE_ENERGY_2 = 1.2;
	private static final double MATE_DESIRE = 40.0;
	private static final double MATE_DESIRE_TO_NORMAL = 65.0;

	private Animal _danger_source;
	private SelectionStrategy _danger_strategy;

	public Sheep(SelectionStrategy mate_strategy, SelectionStrategy danger_strategy, Vector2D pos) {

		super("Sheep", Diet.HERBIVORE, INIT_SIGHT_RANGE, INIT_SPEED, mate_strategy, pos);
		_danger_strategy = danger_strategy;

	}

	protected Sheep(Sheep p1, Animal p2) {

		super(p1, p2);
		_danger_strategy = p1._danger_strategy;
		_danger_source = null;
	}

	@Override
	public void update(double dt) {

		switch (_state) {
		case DEAD:
			return;
		case HUNGER:
			break;
		case NORMAL:
			normal_state(dt);
			break;
		case DANGER:
			danger_state(dt);
			break;
		case MATE:
			mate_state(dt);
			break;
		}

		if (!_pos.within_bounds(_region_mngr.get_width(), _region_mngr.get_height())) {

			_pos.adjust(_region_mngr.get_width(), _region_mngr.get_height());
			state_to_normal();
		}

		if (_energy == 0.0 || _age > DEAD_AGE)
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
		if (_danger_source == null)
			_danger_source = _danger_strategy.select(this,
					_region_mngr.get_animals_in_range(this, animal -> animal.get_diet().equals(Diet.CARNIVORE)));

		if (_danger_source != null)
			state_to_danger();
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

	private void danger_state(double dt) {

		// logic
		if (_danger_source != null && _danger_source.get_state().equals(State.DEAD))
			_danger_source = null;

		if (_danger_source == null)
			normal_state_logic(dt);
		else {

			_dest = _pos.plus(_pos.minus(_danger_source.get_position()).direction());
			super.move(DANGER_MOVE_1 * _speed * dt * Math.exp((_energy - MAX_RATE) * DANGER_MOVE_2));
			_age += dt;

			double energy = _energy - DANGER_ENERGY_1 * DANGER_ENERGY_2 * dt;
			_energy = Utils.constrain_value_in_range(energy, MIN_RATE, MAX_RATE);

			double desire = _desire + DANGER_DESIRE * dt;
			_desire = Utils.constrain_value_in_range(desire, MIN_RATE, MAX_RATE);
		}

		// state change
		if (_danger_source == null || _danger_source.get_position().distanceTo(_pos) > _sight_range) {

			_danger_source = _danger_strategy.select(this,
					_region_mngr.get_animals_in_range(this, animal -> animal.get_diet().equals(Diet.CARNIVORE)));

			if (_danger_source == null) {

				if (_desire < DANGER_DESIRE_TO_NORMAL)
					state_to_normal();
				else
					state_to_mate();
			}
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
					_region_mngr.get_animals_in_range(this, animal -> animal.get_genetic_code().equals("Sheep")));

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
					_baby = new Sheep(this, _mate_target);

				_mate_target = null;

			}
		}

		// state change
		if (_danger_source == null)
			_danger_source = _danger_strategy.select(this,
					_region_mngr.get_animals_in_range(this, animal -> animal.get_diet().equals(Diet.CARNIVORE)));

		if (_danger_source != null)
			state_to_danger();
		else if (_desire < MATE_DESIRE_TO_NORMAL)
			state_to_normal();
	}

	private void state_to_normal() {

		_state = State.NORMAL;
		_danger_source = null;
		_mate_target = null;
	}

	private void state_to_mate() {

		_state = State.MATE;
		_danger_source = null;
	}

	private void state_to_danger() {

		_state = State.DANGER;
		_mate_target = null;
	}
}