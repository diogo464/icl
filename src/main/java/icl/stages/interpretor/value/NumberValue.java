package icl.stages.interpretor.value;

import icl.ValueType;

public class NumberValue extends Value {
    public float value;

    NumberValue(ValueType type, float value) {
        super(type);
        this.value = value;
    }

    public float getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
