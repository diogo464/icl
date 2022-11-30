package icl.stages.interpretor.value;

import icl.ValueType;

public class VoidValue extends Value {
    VoidValue(ValueType type) {
        super(type);
    }

    @Override
    public String toString() {
        return "void";
    }
}
