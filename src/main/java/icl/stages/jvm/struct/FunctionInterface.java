package icl.stages.jvm.struct;

import icl.ValueType;

public class FunctionInterface {
    // Function signature of this interface.
    public final ValueType.Function type;

    // Name of the JVM interface.
    public final String typename;

    // Type descriptor of the interface.
    // This is the JVM type descriptor.
    public final String descriptor;

    // Type descriptor of the call method.
    public final String call_descriptor;

    public FunctionInterface(ValueType.Function type, String typename, String descriptor, String call_descriptor) {
        this.type = type;
        this.typename = typename;
        this.descriptor = descriptor;
        this.call_descriptor = call_descriptor;
    }
}
