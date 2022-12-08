package icl.stages.jvm.struct;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import icl.stages.jvm.JvmUtils;

public class StackFrame {
    // JVM class name;
    public final String typename;

    // Type descriptor of the interface.
    // This is the JVM type descriptor for the stackframe class.
    public final String descriptor;

    // Parent stackframe
    public final Optional<StackFrame> parent;

    // Fields in this stackframe
    public final List<StackFrameField> fields;

    public StackFrame(String typename, Optional<StackFrame> parent, List<StackFrameField> fields) {
        this.typename = typename;
        this.descriptor = JvmUtils.descriptorFromTypename(typename);
        this.parent = parent;
        this.fields = Collections.unmodifiableList(List.copyOf(fields));
    }

    public Optional<StackFrameLookup> lookup(String name) {
        var current = this;
        var depth = 0;
        while (current != null) {
            for (var field : current.fields) {
                if (field.name.equals(name))
                    return Optional.of(new StackFrameLookup(current, field, depth));
            }
            current = current.parent.orElse(null);
            depth++;
        }
        return Optional.empty();
    }
}
