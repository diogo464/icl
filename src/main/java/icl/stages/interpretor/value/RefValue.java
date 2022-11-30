package icl.stages.interpretor.value;

import icl.ValueType;

public class RefValue extends Value {
    Value value;

    RefValue(ValueType type, Value value) {
        super(type);
        this.value = value;

        assert type.equals(ValueType.createReference(value.type));
    }

    public Value getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
