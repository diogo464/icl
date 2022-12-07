package icl.stages.jvm.reference;

import icl.ValueType;
import icl.stages.jvm.JvmUtils;

public class Reference {
    // JVM class name;
    public final String typename;

    // Type descriptor of the interface.
    // This is the JVM type descriptor for the stackframe class.
    public final String descriptor;

    public final ValueType target;

    public Reference(String typename, ValueType target) {
        this.typename = typename;
        this.descriptor = JvmUtils.typedescriptorFromTypename(typename);
        this.target = target;
    }
}
