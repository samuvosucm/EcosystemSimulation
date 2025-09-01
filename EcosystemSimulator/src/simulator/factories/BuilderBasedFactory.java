package simulator.factories;

import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> {

	private Map<String, Builder<T>> _builders;
	private List<JSONObject> _builders_info;

	public BuilderBasedFactory() {

		_builders = new HashMap<>();
		_builders_info = new LinkedList<>();
	}

	public BuilderBasedFactory(List<Builder<T>> builders) {

		this();

		for (Builder<T> b : builders)
			add_builder(b);

	}

	public void add_builder(Builder<T> b) {

		_builders.put(b.get_type_tag(), b);
		_builders_info.add(b.get_info());
	}

	@Override
	public T create_instance(JSONObject info) {

		if (info == null)
			throw new IllegalArgumentException("’info’ cannot be null");

		T builder = null;
		if (_builders.containsKey(info.getString("type"))) {

			JSONObject jo = info.has("data") ? info.getJSONObject("data") : new JSONObject();
			builder = _builders.get(info.get("type")).create_instance(jo);
		}

		if (builder == null)
			throw new IllegalArgumentException("Unrecognized ‘info’:" + info.toString());

		return builder;
	}

	@Override
	public List<JSONObject> get_info() {
		return Collections.unmodifiableList(_builders_info);
	}

}
