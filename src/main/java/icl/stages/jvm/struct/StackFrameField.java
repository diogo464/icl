package icl.stages.jvm.struct;

import icl.ValueType;

public class StackFrameField {
    // Variable of this field.
    // This is the name used in the source code.
    public final String name;

    // Type of the variable in the source code.
    public final ValueType type;

    // Field name in the class.
    // This is the name of the field in the generated class.
    public final String field;

    // Type descriptor of the field.
    // This is the JVM type descriptor used in the generated class.
    public final String descriptor;

    public StackFrameField(String name, ValueType type, String field, String descriptor) {
        this.name = name;
        this.type = type;
        this.field = field;
        this.descriptor = descriptor;
    }
}