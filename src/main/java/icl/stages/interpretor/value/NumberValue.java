package icl.stages.interpretor.value;

import icl.ValueType;

public class NumberValue extends Value {
    public double value;

    NumberValue(ValueType type, double value) {
        super(type);
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
