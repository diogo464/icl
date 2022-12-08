package icl.stages.typecheck;

import icl.ValueType;

public class Variable {
    public final ValueType type;
    public final boolean mutable;

    public Variable(ValueType type, boolean mutable) {
        this.type = type;
        this.mutable = mutable;
    }
}
