package icl.stages.jvm;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import icl.ValueType;

public class Environment {
    private final Context context;
    private final String typename;
    private final Map<String, EnvironmentField> fields;
    private final Optional<Environment> parent;

    public Environment(Context context) {
        this(context, Optional.empty());
    }

    public Environment(Context context, Optional<Environment> parent) {
        this.context = context;
        this.typename = context.generate(Context.Namespace.ENVIRONMENT);
        this.fields = new HashMap<>();
        this.parent = parent;
    }

    public Context getContext() {
        return this.context;
    }

    public String getTypename() {
        return this.typename;
    }

    public String getDescriptor() {
        return Names.typenameToDescriptor(this.getTypename());
    }

    public Iterable<EnvironmentField> fields() {
        return this.fields.values();
    }

    public Environment begin() {
        return new Environment(this.context, Optional.of(this));
    }

    public EnvironmentField define(String name, ValueType vtype) {
        assert !this.fields.containsKey(name);
        var field_name = this.context.generate(Context.Namespace.VARIABLE);
        var descriptor = Names.descriptor(vtype);
        var field = new EnvironmentField(name, vtype, field_name, descriptor);
        this.fields.put(name, field);
        return field;
    }

    public Optional<EnvironmentLookup> lookup(String name) {
        var field = this.fields.get(name);
        if (field != null)
            return Optional.of(new EnvironmentLookup(this, field, 0));

        if (this.hasParent()) {
            var lookup = this.getParent().lookup(name);
            if (lookup.isPresent()) {
                return Optional.of(new EnvironmentLookup(
                        lookup.get().env,
                        lookup.get().field,
                        lookup.get().depth + 1));
            }
        }

        return Optional.empty();
    }

    public Environment end() {
        return this.parent.get();
    }

    public boolean hasParent() {
        return this.parent.isPresent();
    }

    public Environment getParent() {
        assert this.hasParent();
        return this.parent.get();
    }
}
