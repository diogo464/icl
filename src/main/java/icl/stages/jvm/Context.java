package icl.stages.jvm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import icl.ValueType;

public class Context {
    private static enum NameKind {
        STACKFRAME,
        VARIABLE,
        RECORD,
        FUNCTION,
        FUNCTION_INTERFACE,
    }

    private final Map<NameKind, Integer> name_counters;
    private final List<CompiledClass> compiled_classes;
    private final Map<ValueType, String> typenames;

    public Context() {
        this.name_counters = new HashMap<>();
        this.compiled_classes = new ArrayList<>();
        this.typenames = new HashMap<>();
    }

    public String generateStackFrameName() {
        return this.generateName(NameKind.STACKFRAME);
    }

    public String generateFunctionInterfaceName() {
        return this.generateName(NameKind.FUNCTION_INTERFACE);
    }

    private String generateName(NameKind kind) {
        var current = this.name_counters.computeIfAbsent(kind, k -> 0);
        this.name_counters.put(kind, current + 1);
        return kind.name().toLowerCase() + "_" + current;
    }

    public void addCompiledClass(CompiledClass compiledClass) {
        this.compiled_classes.add(compiledClass);
    }

    public List<CompiledClass> getCompiledClasses() {
        return Collections.unmodifiableList(this.compiled_classes);
    }

    public String getFnCallDescriptor(ValueType.Function fn) {
        var builder = new StringBuilder();
        builder.append("(");
        for (var arg : fn.args) {
            builder.append(this.getValueTypeDescriptor(arg));
        }
        builder.append(")");
        builder.append(this.getValueTypeDescriptor(fn.ret));
        return builder.toString();
    }

    public String getValueTypeTypename(ValueType type) {
        if (this.typenames.containsKey(type))
            return this.typenames.get(type);

        switch (type.getKind()) {
            case Alias, Boolean, Number, String, Void -> {
                throw new IllegalArgumentException("Cannot get typename for type: " + type);
            }
            case Function -> {
                var name = this.generateName(NameKind.FUNCTION);
                this.typenames.put(type, name);
                return name;
            }
            case Record -> {
                var name = this.generateName(NameKind.RECORD);
                this.typenames.put(type, name);
                return name;
            }
            case Reference -> {
                var name = this.generateName(NameKind.RECORD);
                this.typenames.put(type, name);
                return name;
            }
            default -> throw new UnsupportedOperationException("Unimplemented case: " + type.getKind());
        }
    }

    public String getValueTypeDescriptor(ValueType type) {
        return switch (type.getKind()) {
            case Boolean -> "I";
            case Function -> JvmUtils.descriptorFromTypename(this.getValueTypeTypename(type));
            case Number -> "I";
            case Record -> JvmUtils.descriptorFromTypename(this.getValueTypeTypename(type));
            case Reference -> JvmUtils.descriptorFromTypename(this.getValueTypeTypename(type));
            case String -> "Ljava/lang/String;";
            case Void -> "Ljava/lang/Object;";
            case Alias -> throw new UnsupportedOperationException("Unimplemented case: " + type.getKind());
        };
    }
}
