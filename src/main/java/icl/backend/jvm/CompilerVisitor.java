package icl.backend.jvm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import icl.ast.AstBinOp;
import icl.ast.AstDecl;
import icl.ast.AstDef;
import icl.ast.AstEmptyNode;
import icl.ast.AstNum;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;

class CompilerVisitor implements AstVisitor {
	private final ClassWriter main_class;
	private final MethodVisitor main_method;
	private final NameGenerator name_generator;
	private final JvmEnvironment environment;
	private final ArrayList<JvmCompiler.CompiledClass> compiled_classes;

	public CompilerVisitor() {
		this.main_class = new ClassWriter(0);
		this.main_class.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Main", null, "java/lang/Object", new String[] {});
		this.main_method = this.main_class.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null,
				null);
		this.main_method.visitCode();
		this.main_method.visitMaxs(256, 256);
		this.main_method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		this.name_generator = new NameGenerator();
		this.environment = new JvmEnvironment(this.name_generator);
		this.compiled_classes = new ArrayList<>();
	}

	public List<JvmCompiler.CompiledClass> finish() {
		this.main_method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;",
				false);
		this.main_method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println",
				"(Ljava/lang/String;)V", false);
		this.main_method.visitInsn(Opcodes.RETURN);
		this.main_method.visitEnd();
		this.main_class.visitEnd();

		var main_class_bytecode = this.main_class.toByteArray();
		var main_class_compiled = new JvmCompiler.CompiledClass("Main", main_class_bytecode);
		this.compiled_classes.add(main_class_compiled);
		return this.compiled_classes;
	}

	@Override
	public void acceptNum(AstNum node) {
		var value = node.value();
		this.main_method.visitIntInsn(Opcodes.SIPUSH, value);
	}

	@Override
	public void acceptBinOp(AstBinOp node) {
		node.left.accept(this);
		node.right.accept(this);
		var opcode = switch (node.kind) {
			case ADD -> Opcodes.IADD;
			case DIV -> Opcodes.IDIV;
			case MUL -> Opcodes.IMUL;
			case SUB -> Opcodes.ISUB;
		};
		this.main_method.visitInsn(opcode);
	}

	@Override
	public void acceptUnaryOp(AstUnaryOp node) {
		switch (node.kind) {
			case POS -> {
				return;
			}
			case NEG -> {
				this.main_method.visitInsn(Opcodes.INEG);
			}
		}
	}

	@Override
	public void acceptDecl(AstDecl node) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void acceptDef(AstDef node) {
		var varcount = node.decls.size();

	}

	@Override
	public void acceptEmptyNode(AstEmptyNode node) {
	}

	@Override
	public void acceptVar(AstVar node) {
		// TODO Auto-generated method stub

	}

}
