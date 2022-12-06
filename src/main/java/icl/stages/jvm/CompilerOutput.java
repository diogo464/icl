package icl.stages.jvm;

import java.util.Collections;
import java.util.List;

public class CompilerOutput {
    public final List<CompiledClass> classes;

    public CompilerOutput(List<CompiledClass> classes) {
        this.classes = Collections.unmodifiableList(List.copyOf(classes));
    }
}
