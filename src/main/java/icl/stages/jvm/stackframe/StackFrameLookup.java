package icl.stages.jvm.stackframe;

public class StackFrameLookup {
    public final StackFrame frame;
    public final StackFrameField field;
    public final int depth;

    public StackFrameLookup(StackFrame frame, StackFrameField field, int depth) {
        this.frame = frame;
        this.field = field;
        this.depth = depth;
    }

    @Override
    public String toString() {
        return "StackFrameLookup [frame=" + frame + ", field=" + field + ", depth=" + depth + "]";
    }
}
