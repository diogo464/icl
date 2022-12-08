package icl.stages.jvm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import icl.ast.AnnotationKey;
import icl.ast.AstNode;
import icl.stages.jvm.struct.Reference;
import icl.stages.jvm.struct.StackFrame;
import icl.stages.jvm.struct.Function;
import icl.stages.jvm.struct.FunctionInterface;
import icl.stages.jvm.struct.Record;

/// StackFrame
// A stack frame is a class that stores local variables as fields.
// There is a special field `parent` that stores the parent stack frame.

/// Closure/Function
// A function captures the environment in which it was defined.
// A function is a class that stores the stack frame of the environment as a field `frame`.
// For every function signature there is a corresponding interface that contains
// a method `call`. This method takes the arguments as parameters and returns the
// result. 
// Every function implements this interface.

/// Reference
// A reference is a class with a single field `value` that stores a given value.
// For every value type there is a corresponding reference class.

/// Record
// A record is a class that stores the fields of the record as fields.

// TODO: Merge NameGenerator and Context

public class Compiler {
	public static final int SL_INDEX = 3;

	// public static AnnotationKey<StackFrame> STACK_FRAME_KEY = new
	// AnnotationKey<>("stackframe");

	/**
	 * Compiles a Main class with a main method.
	 * 
	 * @param context The context
	 * @param node    The node to compile
	 * @return The compiled class
	 */
	static CompiledClass main(Context context, AstNode node) {
		var main_class = new ClassWriter(0);
		main_class.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Main", null,
				"java/lang/Object", new String[] {});
		var method = main_class.visitMethod(Opcodes.ACC_PUBLIC +
				Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null,
				null);
		method.visitCode();
		method.visitMaxs(256, 256);

		compile(context, node, method);

		method.visitInsn(Opcodes.RETURN);
		method.visitEnd();
		main_class.visitEnd();

		return new CompiledClass("Main", main_class.toByteArray());
	}

	/**
	 * Compiles an AstNode
	 * 
	 * @param context The context
	 * @param node    The node to compile
	 * @param method  The method this node is compiled into
	 */
	static void compile(Context context, AstNode node, MethodVisitor method) {
		var visitor = new CompilerVisitor(context, method);
		node.accept(visitor);
	}

	/**
	 * Compiles a Function
	 * 
	 * @param context  The context
	 * @param function The function to compile
	 * @return The compiled class
	 */
	public static CompiledClass compile(Context context, Function function) {
		var cwriter = new ClassWriter(0);
		cwriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, function.typename, null, "java/lang/Object", new String[] {
				function.iface.descriptor
		});
		cwriter.visitField(Opcodes.ACC_PUBLIC, "frame", function.environment.descriptor, null, null);

		var method = cwriter.visitMethod(Opcodes.ACC_PUBLIC, "call", function.iface.call_descriptor, null, null);
		method.visitCode();
		method.visitMaxs(256, 256);

		// Load frame field into SL INDEX
		method.visitVarInsn(Opcodes.ALOAD, 0);
		method.visitFieldInsn(Opcodes.GETFIELD, function.typename, "frame", function.environment.descriptor);
		method.visitVarInsn(Opcodes.ASTORE, SL_INDEX);
		var visitor = new CompilerVisitor(context, method, function.environment);
		function.node.accept(visitor);
		method.visitInsn(Opcodes.RETURN);
		method.visitEnd();

		return new CompiledClass(function.typename, cwriter.toByteArray());
	}

	/**
	 * Compiles a FunctionInterface
	 * 
	 * @param context    The context
	 * @param finterface The function interface to compile
	 * @return The compiled class
	 */
	public static CompiledClass compile(Context context, FunctionInterface finterface) {
		var cwriter = new ClassWriter(0);
		cwriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_INTERFACE, finterface.typename, null,
				"java/lang/Object", new String[] {});

		var method = cwriter.visitMethod(Opcodes.ACC_PUBLIC, "call", finterface.call_descriptor, null, null);
		method.visitEnd();
		cwriter.visitEnd();

		return new CompiledClass(finterface.typename, cwriter.toByteArray());
	}

	/**
	 * Compiles a Record
	 * 
	 * @param context The context
	 * @param record  The record to compile
	 * @return The compiled class
	 */
	public static CompiledClass compile(Context context, Record record) {
		var cwriter = new ClassWriter(0);
		cwriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, record.typename, null, "java/lang/Object", new String[] {});

		for (var field : record.type.fields()) {
			var name = field.getKey();
			var type = field.getValue();
			var descriptor = context.getValueTypeDescriptor(type);
			cwriter.visitField(Opcodes.ACC_PUBLIC, name, descriptor, null, null);
		}

		generateDefaultInitMethod(cwriter);
		cwriter.visitEnd();
		return new CompiledClass(record.typename, cwriter.toByteArray());
	}

	/**
	 * Compiles a Reference
	 * 
	 * @param context   The context
	 * @param reference The reference to compile
	 * @return The compiled class
	 */
	static CompiledClass compile(Context context, Reference reference) {
		var cwriter = new ClassWriter(0);
		cwriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, reference.typename, null, "java/lang/Object", new String[] {});
		cwriter.visitField(Opcodes.ACC_PUBLIC, "value", context.getValueTypeDescriptor(reference.target), null, null);

		generateDefaultInitMethod(cwriter);
		cwriter.visitEnd();
		var bytecode = cwriter.toByteArray();
		return new CompiledClass(reference.typename, bytecode);
	}

	/**
	 * Creates a new object with the given type and calls the init method.
	 * The object is left on the stack.
	 * 
	 * @param method   The method to compile into
	 * @param typename The type of the object to create
	 */
	static void compileBasicNew(MethodVisitor method, String typename) {
		method.visitTypeInsn(Opcodes.NEW, typename);
		method.visitInsn(Opcodes.DUP);
		method.visitMethodInsn(Opcodes.INVOKESPECIAL, typename,
				"<init>", "()V", false);
	}

	private static void generateDefaultInitMethod(ClassWriter frame) {
		var init = frame.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		init.visitCode();
		init.visitMaxs(1, 1);
		init.visitVarInsn(Opcodes.ALOAD, 0);
		init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		init.visitInsn(Opcodes.RETURN);
		init.visitEnd();
	}

	public static CompiledClass compile(StackFrame stackframe) {
		var cwriter = new ClassWriter(0);
		cwriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, stackframe.typename, null, "java/lang/Object", new String[] {});

		if (stackframe.parent.isEmpty())
			cwriter.visitField(Opcodes.ACC_PUBLIC, "parent", "Ljava/lang/Object;", null, null);
		else
			cwriter.visitField(Opcodes.ACC_PUBLIC, "parent", stackframe.parent.get().descriptor, null, null);
		for (var field : stackframe.fields)
			cwriter.visitField(Opcodes.ACC_PUBLIC, field.field, field.descriptor, null, null);

		generateDefaultInitMethod(cwriter);
		cwriter.visitEnd();
		return new CompiledClass(stackframe.typename, cwriter.toByteArray());
	}

}
