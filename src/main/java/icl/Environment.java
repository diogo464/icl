package icl;

import java.util.HashMap;

public class Environment<T> {
	private final Environment<T> parent;
	private final HashMap<String, T> values;

	public Environment() {
		this(null);
	}

	private Environment(Environment<T> parent) {
		this.parent = parent;
		this.values = new HashMap<>();
	}

	public Environment<T> beginScope() {
		return new Environment<>(this);
	}

	public Environment<T> endScope() {
		return this.parent;
	}

	public void define(String name, T value) {
		if (this.values.containsKey(name))
			throw new RuntimeException("Variable " + name + " already defined");
		this.values.put(name, value);
	}

	/**
	 * Finds the value associated with the given name.
	 * If no value is found, null is returned.
	 * 
	 * @param name
	 * @return T | null
	 */
	public T lookup(String name) {
		if (this.values.containsKey(name))
			return this.values.get(name);
		if (this.parent != null)
			return this.parent.lookup(name);
		return null;
	}

	@Override
	public String toString() {
		var builder = new StringBuilder();
		for (var entry : this.values.entrySet()) {
			builder.append(entry.getKey());
			builder.append(" = ");
			builder.append(entry.getValue().toString());
			builder.append("\n");
		}
		builder.append("-----------------");
		if (this.parent != null)
			builder.append(this.parent.toString());
		return builder.toString();
	}
}
