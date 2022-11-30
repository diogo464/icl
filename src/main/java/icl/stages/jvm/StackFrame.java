package icl.stages.jvm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

class StackFrame {
	public static final String PARENT_VARNAME = "parent";

	private final String typename;
	private final StackFrame parent;
	private final List<String> variables;

	StackFrame(String typename, StackFrame parent) {
		this.typename = typename;
		this.parent = parent;
		this.variables = new ArrayList<>();
	}

	public String getTypename() {
		return this.typename;
	}

	/**
	 * @return {@link StackFrame} The parent stackframe or null.
	 */
	public StackFrame getParent() {
		return this.parent;
	}

	public String getParentTypeName() {
		if (this.parent != null)
			return this.parent.getTypename();
		return "java/lang/Object";
	}

	public void addVariable(String variable) {
		this.variables.add(variable);
	}

	public JvmCompiler.CompiledClass compile() {
		var writer = new ClassWriter(0);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, this.typename, null,
				"java/lang/Object", null);

		if (parent == null)
			writer.visitField(Opcodes.ACC_PUBLIC, PARENT_VARNAME, "Ljava/lang/Object;", null, null);
		else
			writer.visitField(Opcodes.ACC_PUBLIC, PARENT_VARNAME, "L" + this.parent.getTypename() + ";", null, null);

		for (var variable : this.variables)
			writer.visitField(Opcodes.ACC_PUBLIC, variable, "I", null, null);

		var init = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		init.visitCode();
		init.visitMaxs(1, 1);
		init.visitVarInsn(Opcodes.ALOAD, 0);
		init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		init.visitInsn(Opcodes.RETURN);
		init.visitEnd();

		writer.visitEnd();
		return new JvmCompiler.CompiledClass(this.typename, writer.toByteArray());
	}
}
