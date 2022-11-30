package icl.stages.jvm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import icl.ast.AstAssign;
import icl.ast.AstBinOp;
import icl.ast.AstBool;
import icl.ast.AstCall;
import icl.ast.AstDecl;
import icl.ast.AstScope;
import icl.ast.AstTypeAlias;
import icl.ast.AstEmptyNode;
import icl.ast.AstField;
import icl.ast.AstFn;
import icl.ast.AstIf;
import icl.ast.AstLoop;
import icl.ast.AstNew;
import icl.ast.AstNum;
import icl.ast.AstPrint;
import icl.ast.AstRecord;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;
import icl.stages.typecheck.TypeCheckStage;

class Visitor implements AstVisitor {
	private static final int SL_INDEX = 3;

	private final JvmEnvironment environment;

	// Code generation
	private final ClassWriter main_class;
	private final MethodVisitor visitor;

	public Visitor() {
		var name_generator = new NameGenerator();
		this.environment = new JvmEnvironment(name_generator);

		this.main_class = new ClassWriter(0);
		this.main_class.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Main", null,
				"java/lang/Object", new String[] {});
		this.visitor = this.main_class.visitMethod(Opcodes.ACC_PUBLIC +
				Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null,
				null);
		this.visitor.visitCode();
		this.visitor.visitMaxs(256, 256);
		this.visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
				"Ljava/io/PrintStream;");
	}

	public List<JvmCompiler.CompiledClass> finish() {
		var compiled_classes = new ArrayList<JvmCompiler.CompiledClass>();
		this.visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String",
				"valueOf", "(I)Ljava/lang/String;",
				false);
		this.visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
				"println",
				"(Ljava/lang/String;)V", false);
		this.visitor.visitInsn(Opcodes.RETURN);
		this.visitor.visitEnd();
		this.main_class.visitEnd();

		var main_class_bytecode = this.main_class.toByteArray();
		var main_class_compiled = new JvmCompiler.CompiledClass("Main",
				main_class_bytecode);
		compiled_classes.add(main_class_compiled);

		for (var stackframe : this.environment.getStackFrames())
			compiled_classes.add(stackframe.compile());

		return compiled_classes;
	}

	@Override
	public void acceptNum(AstNum node) {
		var value = node.value;
		this.visitor.visitIntInsn(Opcodes.SIPUSH, value);
	}

	@Override
	public void acceptBool(AstBool node) {
		var value = node.value;
		var ivalue = value ? 1 : 0;
		this.visitor.visitIntInsn(Opcodes.SIPUSH, ivalue);
	}

	@Override
	public void acceptBinOp(AstBinOp node) {
		var operand_type = node.left.getAnnotation(TypeCheckStage.TYPE_KEY);
		node.left.accept(this);
		node.right.accept(this);
		switch (operand_type.getKind()) {
			case Boolean -> {
				switch (node.kind) {
					case CMP -> {
						// Label test_label = new Label();
					}
					case LAND -> {
					}
					case LOR -> {
					}
					default -> throw new IllegalStateException();
				}
			}
			case Number -> {
				switch (node.kind) {
					case ADD -> {
					}
					case SUB -> {
					}
					case MUL -> {
					}
					case DIV -> {
					}
					case CMP -> {
					}
					case GT -> {
					}
					case GTE -> {
					}
					case LT -> {
					}
					case LTE -> {
					}
					default -> throw new IllegalStateException();
				}
			}
			default -> throw new IllegalStateException();
		}
	}

	@Override
	public void acceptUnaryOp(AstUnaryOp node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptDecl(AstDecl node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptScope(AstScope node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptEmptyNode(AstEmptyNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptVar(AstVar node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptCall(AstCall call) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptIf(AstIf astIf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptLoop(AstLoop loop) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptAssign(AstAssign assign) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptPrint(AstPrint print) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptNew(AstNew anew) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptFn(AstFn fn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptRecord(AstRecord record) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptField(AstField field) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptTypeAlias(AstTypeAlias typeAlias) {
		// TODO Auto-generated method stub

	}

}

// class Visitor implements AstVisitor {
// private static final int SL_INDEX = 3;
//
// private final JvmEnvironment environment;
//
// // Code generation
// private final ClassWriter main_class;
// private final MethodVisitor method;
//
// public Visitor() {
// var name_generator = new NameGenerator();
// this.environment = new JvmEnvironment(name_generator);
//
// this.main_class = new ClassWriter(0);
// this.main_class.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Main", null,
// "java/lang/Object", new String[] {});
// this.method = this.main_class.visitMethod(Opcodes.ACC_PUBLIC +
// Opcodes.ACC_STATIC, "main",
// "([Ljava/lang/String;)V", null,
// null);
// this.method.visitCode();
// this.method.visitMaxs(256, 256);
// this.method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
// "Ljava/io/PrintStream;");
// }
//
// public List<JvmCompiler.CompiledClass> finish() {
// var compiled_classes = new ArrayList<JvmCompiler.CompiledClass>();
// this.method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String",
// "valueOf", "(I)Ljava/lang/String;",
// false);
// this.method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
// "println",
// "(Ljava/lang/String;)V", false);
// this.method.visitInsn(Opcodes.RETURN);
// this.method.visitEnd();
// this.main_class.visitEnd();
//
// var main_class_bytecode = this.main_class.toByteArray();
// var main_class_compiled = new JvmCompiler.CompiledClass("Main",
// main_class_bytecode);
// compiled_classes.add(main_class_compiled);
//
// for (var stackframe : this.environment.getStackFrames())
// compiled_classes.add(stackframe.compile());
//
// return compiled_classes;
// }
//
// @Override
// public void acceptNum(AstNum node) {
// var value = node.value;
// this.method.visitIntInsn(Opcodes.SIPUSH, value);
// }
//
// @Override
// public void acceptBinOp(AstBinOp node) {
// node.left.accept(this);
// node.right.accept(this);
// var opcode = switch (node.kind) {
// case ADD -> Opcodes.IADD;
// case DIV -> Opcodes.IDIV;
// case MUL -> Opcodes.IMUL;
// case SUB -> Opcodes.ISUB;
// // TODO: implement others
// default -> throw new IllegalArgumentException("Unexpected value: " +
// node.kind);
// };
// this.method.visitInsn(opcode);
// }
//
// @Override
// public void acceptUnaryOp(AstUnaryOp node) {
// switch (node.kind) {
// case POS -> {
// }
// case NEG -> this.method.visitInsn(Opcodes.INEG);
// // TODO: implement others
// default -> throw new IllegalArgumentException("Unexpected value: " +
// node.kind);
// }
// }
//
// @Override
// public void acceptDecl(AstDecl node) {
// throw new UnsupportedOperationException();
// }
//
// @Override
// public void acceptScope(AstScope node) {
// this.environment.beginScope();
// var stackframe = this.environment.getCurrentStackFrame();
// this.method.visitTypeInsn(Opcodes.NEW, stackframe.getTypename());
// this.method.visitInsn(Opcodes.DUP);
// this.method.visitMethodInsn(Opcodes.INVOKESPECIAL, stackframe.getTypename(),
// "<init>", "()V", false);
//
// // Assign the current stackframe as the parent
// var parent_frame = stackframe.getParent();
// if (parent_frame != null) {
// this.method.visitInsn(Opcodes.DUP);
// this.method.visitVarInsn(Opcodes.ALOAD, SL_INDEX);
// this.method.visitFieldInsn(Opcodes.PUTFIELD, stackframe.getTypename(),
// StackFrame.PARENT_VARNAME,
// "L" + parent_frame.getTypename() + ";");
// }
//
// // Assign the current stackframe to the SL variable
// this.method.visitInsn(Opcodes.DUP);
// this.method.visitVarInsn(Opcodes.ASTORE, SL_INDEX);
// for (var decl : node.stmts) {
// //// Push a copy of the stackframe to the top of the stack
// // this.method.visitInsn(Opcodes.DUP);
// //// Push the value of this variable to the top of the stack
// // decl.value.accept(this);
// // var variable = this.environment.define(decl.name);
// // this.method.visitFieldInsn(Opcodes.PUTFIELD,
// // variable.stackframe.getTypename(), variable.name, "I");
// }
// this.method.visitInsn(Opcodes.POP);
// node.expr.accept(this);
//
// if (this.environment.getCurrentDepth() != 0) {
// this.compilePushStackFrame(this.environment.getCurrentDepth() - 1);
// this.method.visitVarInsn(Opcodes.ASTORE, SL_INDEX);
// }
//
// this.environment.endScope();
// }
//
// @Override
// public void acceptEmptyNode(AstEmptyNode node) {
// }
//
// @Override
// public void acceptVar(AstVar node) {
// var variable = this.environment.lookup(node.name);
// if (variable == null)
// throw new RuntimeException(
// "Variable " + node.name + " at line " + node.location.beginLine + " is not
// defined");
// this.compilePushStackFrame(variable.depth);
// this.method.visitFieldInsn(Opcodes.GETFIELD,
// variable.stackframe.getTypename(), variable.name, "I");
// }
//
// /**
// * Push a reference to the stackframe at depth to the top of the stack.
// *
// * @param depth
// */
// private void compilePushStackFrame(int depth) {
// if (depth > this.environment.getCurrentDepth())
// throw new IllegalStateException("Attempt to load stackframe at depth greater
// the current maximum depth");
// var current_depth = this.environment.getCurrentDepth();
// var current_frame = this.environment.getCurrentStackFrame();
// this.method.visitVarInsn(Opcodes.ALOAD, SL_INDEX);
// while (current_depth != depth) {
// var parent_frame_typename = current_frame.getParentTypeName();
// this.method.visitFieldInsn(Opcodes.GETFIELD, current_frame.getTypename(),
// StackFrame.PARENT_VARNAME,
// "L" + parent_frame_typename + ";");
// current_frame = current_frame.getParent();
// current_depth -= 1;
// }
// }
// }
