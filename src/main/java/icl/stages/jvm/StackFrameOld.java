package icl.stages.jvm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

class StackFrameOld {
	public static final String PARENT_VARNAME = "parent";

	private final String typename;
	private final StackFrameOld parent;
	private final List<StackFrameField> fields;

	StackFrameOld(String typename, StackFrameOld parent) {
		this.typename = typename;
		this.parent = parent;
		this.fields = new ArrayList<>();
	}

	public String getTypename() {
		return this.typename;
	}

	/**
	 * @return {@link StackFrameOld} The parent stackframe or null.
	 */
	public StackFrameOld getParent() {
		return this.parent;
	}

	public String getParentTypeName() {
		if (this.parent != null)
			return this.parent.getTypename();
		return "java/lang/Object";
	}

	public void define(String name, String descriptor) {
		this.fields.add(new StackFrameField(name, descriptor));
	}

	public CompiledClass compile() {
		var writer = new ClassWriter(0);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, this.typename, null,
				"java/lang/Object", null);

		if (parent == null)
			writer.visitField(Opcodes.ACC_PUBLIC, PARENT_VARNAME, "Ljava/lang/Object;", null, null);
		else
			writer.visitField(Opcodes.ACC_PUBLIC, PARENT_VARNAME, "L" + this.parent.getTypename() + ";", null, null);

		for (var field : this.fields)
			writer.visitField(Opcodes.ACC_PUBLIC, field.name, field.descriptor, null, null);

		var init = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		init.visitCode();
		init.visitMaxs(1, 1);
		init.visitVarInsn(Opcodes.ALOAD, 0);
		init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		init.visitInsn(Opcodes.RETURN);
		init.visitEnd();

		writer.visitEnd();
		return new CompiledClass(this.typename, writer.toByteArray());
	}
}
