package icl;

import java.util.HashMap;
import java.util.Optional;

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

    public Optional<T> lookup(String name) {
        if (this.values.containsKey(name))
            return Optional.of(this.values.get(name));
        if (this.parent != null)
            return this.parent.lookup(name);
        return Optional.empty();
    }
}
