package icl.stages.jvm2;

import icl.ValueType;

public class EnvironmentField {
    public final String name;
    public final ValueType type;
    public final String field;
    public final String descriptor;

    public EnvironmentField(String name, ValueType type, String field, String descriptor) {
        this.name = name;
        this.type = type;
        this.field = field;
        this.descriptor = descriptor;
    }
}
