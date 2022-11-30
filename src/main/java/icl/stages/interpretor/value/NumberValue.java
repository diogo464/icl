package icl.stages.interpretor.value;

import icl.ValueType;

public class NumberValue extends Value {
    public short value;

    NumberValue(ValueType type, short value) {
        super(type);
        this.value = value;
    }

    public short getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
