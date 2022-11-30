package icl.stages.interpretor.value;

import java.util.Map;

import icl.ValueType;

public class RecordValue extends Value {

    Map<String, Value> fields;

    protected RecordValue(ValueType type, Map<String, Value> fields) {
        super(type);
        this.fields = fields;
    }

    public Value getField(String name) {
        if (!fields.containsKey(name))
            throw new RuntimeException("Field " + name + " not found in record " + this);
        return this.fields.get(name);
    }

    @Override
    public String toString() {
        return "RecordValue [fields=" + fields + "]";
    }

}
