package icl.stages.jvm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import icl.stages.jvm.stackframe.StackFrame;

public class Context {
    private final Set<StackFrame> stackframes;

    public Context() {
        this.stackframes = new HashSet<>();
    }

    public void registerStackFrame(StackFrame frame) {
        this.stackframes.add(frame);
    }

    public List<StackFrame> getStackFrames() {
        return List.copyOf(this.stackframes);
    }
}
