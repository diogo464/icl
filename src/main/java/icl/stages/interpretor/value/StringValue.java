package icl.stages.interpretor.value;

import icl.ValueType;

public class StringValue extends Value {
    String value;

    public StringValue(ValueType type, String value) {
        super(type);
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

}
