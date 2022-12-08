package icl.stages.typecheck;

import icl.Environment;
import icl.ValueType;

public class TypeCheckEnv {
    // Value namespace
    public final Environment<Variable> value;
    // Type namespace
    public final Environment<ValueType> type;

    public TypeCheckEnv(Environment<Variable> value, Environment<ValueType> type) {
        this.value = value;
        this.type = type;
    }

    public TypeCheckEnv() {
        this.value = new Environment<>();
        this.type = new Environment<>();
    }

    public TypeCheckEnv beginScope() {
        return new TypeCheckEnv(value.beginScope(), type.beginScope());
    }
}
