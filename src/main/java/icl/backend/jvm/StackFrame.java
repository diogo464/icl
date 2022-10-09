package icl.backend.jvm;

import java.util.Optional;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

class StackFrame {
    private final String typename;
    private final int varcount;
    private final String[] varnames;
    private final Optional<StackFrame> parent;

    StackFrame(NameGenerator name_generator, String typename, int varcount, Optional<StackFrame> parent) {
        this.typename = typename;
        this.varcount = varcount;
        this.varnames = new String[varcount];
        this.parent = parent;

        for (int i = 0; i < varcount; i++)
            this.varnames[i] = name_generator.generateVariableName();
    }

    public JvmCompiler.CompiledClass compile() {
        // var writer = new ClassWriter(0);
        // writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, this.typename, null,
        // "java/lang/Object", null);

        // if (this.parent.isPresent()) {
        // writer.visitField(Opcodes.ACC_PUBLIC, "parent", "L" +
        // this.parent.get().typename + ";", null, null);
        // }
        // for (var i = 0; i < this.varcount; ++i)
        // writer.visitField(Opcodes.ACC_PUBLIC, this.varnames[i], "I", null, null);

        // var create_descriptor = "(";
        // boolean add_comma = false;
        // if (this.parent.isPresent()) {
        // add_comma = true;
        // writer.visitField(Opcodes.ACC_PUBLIC, "parent", "L" +
        // this.parent.get().typename + ";", null, null);
        // }
        // for (var i = 0; i < this.varcount; i++) {
        // if (add_comma) {
        // create_descriptor += ",";
        // }
        // create_descriptor += "I";
        // add_comma = true;
        // }
        // create_descriptor += ")L" + this.typename + ";";

        // writer.visitMethod(Opcodes.ACC_STATIC + Opcodes.ACC_PUBLIC, "create",
        // create_descriptor, null, null);
        return null;
    }
}
