package icl.stages.jvm;

import java.util.ArrayList;
import java.util.List;

public class StackFrameBuilder {
    private final String typename;
    private final StackFrameOld parent;
    private final List<StackFrameField> fields;

    public StackFrameBuilder(NameGenerator nameGenerator, StackFrameOld parent) {
        this.typename = nameGenerator.generateStackFrameName();
        this.parent = parent;
        this.fields = new ArrayList<>();
    }

}
