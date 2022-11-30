package icl.stages.interpretor.value;

import icl.ValueType;

public class BooleanValue extends Value {
    public boolean value;

    BooleanValue(ValueType type, boolean value) {
        super(type);
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
