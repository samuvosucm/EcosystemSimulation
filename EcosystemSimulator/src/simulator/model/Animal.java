package simulator.model;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {

	private static final double INIT_SPEED_TOLERANCE = 0.1;
	protected static final double MIN_RATE = 0.0;
	protected static final double MAX_RATE = 100.0;
	protected static final double CLOSING_GAP_RANGE = 8.0;
	protected static final double BABY_PROBABILITY = 0.9;
	private static final double INIT_ENERGY = 100.0;

	protected String _genetic_code;
	protected Diet _diet;
	protected State _state;
	protected Vector2D _pos;
	protected Vector2D _dest;
	protected double _energy;
	protected double _speed;
	protected double _age;
	protected double _desire;
	protected double _sight_range;
	protected Animal _mate_target;
	protected Animal _baby;
	protected AnimalMapView _region_mngr;
	protected SelectionStrategy _mate_strategy;

	protected Animal(String genetic_code, Diet diet, double sight_range, double init_speed,
			SelectionStrategy mate_strategy, Vector2D pos) {

		if (genetic_code.isEmpty())
			throw new IllegalArgumentException("genetic_code can't be empty");
		if (sight_range < 0.0)
			throw new IllegalArgumentException("sight_range can't be negative");
		if (init_speed < 0.0)
			throw new IllegalArgumentException("init_speed can't be negative");
		if (mate_strategy == null)
			throw new IllegalArgumentException("mate_strategy can't be null");

		_genetic_code = genetic_code;
		_diet = diet;
		_sight_range = sight_range;
		_pos = pos;
		_mate_strategy = mate_strategy;
		_speed = Utils.get_randomized_parameter(init_speed, INIT_SPEED_TOLERANCE);
		_state = State.NORMAL;
		_energy = INIT_ENERGY;
		_desire = 0.0;
		_dest = null;
		_mate_target = null;
		_baby = null;
		_region_mngr = null;
	}

	protected Animal(Animal p1, Animal p2) {

		_dest = null;
		_baby = null;
		_mate_target = null;
		_region_mngr = null;
		_state = State.NORMAL;
		_desire = 0.0;
		_genetic_code = p1.get_genetic_code();
		_diet = p1.get_diet();
		_mate_strategy = p2.get_mate_strategy();
		_energy = (p1.get_energy() + p2.get_energy()) / 2;
		_pos = p1.get_position().plus(Vector2D.get_random_vector(-1, 1).scale(60.0 * (Utils._rand.nextGaussian() + 1)));
		_sight_range = Utils.get_randomized_parameter((p1.get_sight_range() + p2.get_sight_range()) / 2, 0.2);
		_speed = Utils.get_randomized_parameter((p1.get_speed() + p2.get_speed()) / 2, 0.2);
	}

	@Override
	public State get_state() {
		return _state;
	}

	@Override
	public Vector2D get_position() {
		return _pos;
	}

	@Override
	public String get_genetic_code() {
		return _genetic_code;
	}

	@Override
	public Diet get_diet() {
		return _diet;
	}

	@Override
	public double get_speed() {
		return _speed;
	}

	@Override
	public double get_sight_range() {
		return _sight_range;
	}

	@Override
	public double get_energy() {
		return _energy;
	}

	@Override
	public double get_age() {
		return _age;
	}

	@Override
	public Vector2D get_destination() {
		return _dest;
	}

	@Override
	public boolean is_pregnant() {
		return _baby != null;
	}

	public void set_desire(double desire) {
		_desire = desire;
	}

	public void set_state(State state) {
		_state = state;
	}

	public SelectionStrategy get_mate_strategy() {
		return _mate_strategy;
	}

	void init(AnimalMapView reg_mngr) {

		_region_mngr = reg_mngr;
		double width = _region_mngr.get_width();
		double height = _region_mngr.get_height();

		if (_pos == null) {

			double x = Utils._rand.nextDouble(width);
			double y = Utils._rand.nextDouble(height);
			_pos = new Vector2D(x, y);
		} else if (!_pos.within_bounds(width, height))
			_pos.adjust(width, height);

		double x = Utils._rand.nextDouble(width);
		double y = Utils._rand.nextDouble(height);
		_dest = new Vector2D(x, y);
	}

	public Animal deliver_baby() {

		Animal baby = _baby;
		_baby = null;
		return baby;
	}

	protected void move(double speed) {

		_pos = _pos.plus(_dest.minus(_pos).direction().scale(speed));
	}

	public JSONObject as_JSON() {

		JSONObject animal_jo = new JSONObject();

		animal_jo.put("pos", _pos.asJSONArray());
		animal_jo.put("gcode", _genetic_code);
		animal_jo.put("diet", _diet);
		animal_jo.put("state", _state);

		return animal_jo;
	}
}