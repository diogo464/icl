package icl.stages.jvm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import icl.ValueType;
import icl.stages.jvm.reference.Reference;
import icl.stages.jvm.stackframe.StackFrame;

public class Context {
    private final NameGenerator nameGenerator;
    private final Set<StackFrame> stackframes;
    private final Map<ValueType, Reference> references;

    public Context(NameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
        this.stackframes = new HashSet<>();
        this.references = new HashMap<>();
    }

    public StackFrame registerStackFrame(StackFrame frame) {
        this.stackframes.add(frame);
        return frame;
    }

    public List<StackFrame> getStackFrames() {
        return List.copyOf(this.stackframes);
    }

    public Reference registerReference(ValueType target) {
        var current = this.references.get(target);
        if (current != null)
            return current;

        var reference = new Reference(this.nameGenerator.generateReferenceName(), target);
        this.references.put(target, reference);
        return reference;
    }

    public List<Reference> getReferences() {
        return List.copyOf(this.references.values());
    }

    public String descriptorFromValueType(ValueType type) {
        return switch (type.getKind()) {
            case Boolean -> "I";
            // TODO: Function descriptors
            case Function -> throw new UnsupportedOperationException();
            case Number -> "I";
            // TODO: Record descriptors
            case Record -> throw new UnsupportedOperationException();
            case Reference -> this.registerReference(type.getReference().target).descriptor;
            case String -> "Ljava/lang/String;";
            case Void -> "Ljava/lang/Object;";
            default -> throw new IllegalArgumentException("Unknown type kind: " + type.getKind());
        };
    }
}
