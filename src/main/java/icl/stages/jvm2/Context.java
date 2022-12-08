package icl.stages.jvm2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import icl.ValueType;
import icl.ast.AstRecord;

public class Context {
    public static enum Namespace {
        STACKFRAME,
        VARIABLE,
        RECORD,
        FUNCTION,
        FUNCTION_INTERFACE,
        ENVIRONMENT,
    }

    private final Set<Environment> compiled_environments;
    private final Set<ValueType.Function> compiled_function_interfaces;
    private final Set<ValueType.Record> compiled_records;
    private final Set<ValueType.Reference> compiled_references;
    private final List<CompiledClass> compiled_classes;
    private final Map<Namespace, Integer> name_counters;

    public Context() {
        this.compiled_environments = new HashSet<>();
        this.compiled_function_interfaces = new HashSet<>();
        this.compiled_records = new HashSet<>();
        this.compiled_references = new HashSet<>();
        this.compiled_classes = new ArrayList<>();
        this.name_counters = new HashMap<>();
    }

    public void emit(CompiledClass compiled_class) {
        this.compiled_classes.add(compiled_class);
    }

    public String generate(Namespace kind) {
        var current = this.name_counters.computeIfAbsent(kind, k -> 0);
        this.name_counters.put(kind, current + 1);
        return kind.name().toLowerCase() + "_" + current;
    }

    public void compile(Environment env) {
        if (this.compiled_environments.contains(env))
            throw new RuntimeException("Environment already compiled");
        this.compiled_environments.add(env);
        var compiled = Compiler.compile(env);
        this.emit(compiled);
    }

    public void compile(ValueType.Function fn) {
        if (this.compiled_function_interfaces.contains(fn))
            return;
        this.compiled_function_interfaces.add(fn);
        var compiled = Compiler.compile(fn);
        this.emit(compiled);
    }

    public void compile(ValueType.Record record) {
        if (this.compiled_records.contains(record))
            return;
        this.compiled_records.add(record);
        var compiled = Compiler.compile(record);
        this.emit(compiled);
    }

    public void compile(ValueType.Reference ref) {
        if (this.compiled_references.contains(ref))
            return;
        this.compiled_references.add(ref);
        var compiled = Compiler.compile(ref);
        this.emit(compiled);
    }

    public List<CompiledClass> classes() {
        return List.copyOf(this.compiled_classes);
    }
}
