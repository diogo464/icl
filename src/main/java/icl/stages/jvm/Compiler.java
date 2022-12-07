package icl.stages.jvm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import icl.ast.AnnotationKey;
import icl.ast.AstNode;
import icl.pipeline.Pipeline;
import icl.stages.jvm.reference.Reference;
import icl.stages.jvm.stackframe.StackFrame;
import icl.stages.jvm.stackframe.StackFrameStage;
import icl.stages.typecheck.TypeCheckStage;

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

// TODO: Remove this.pushVoid
// TODO: Merge NameGenerator and Context

public class Compiler {
	public static AnnotationKey<StackFrame> STACK_FRAME_KEY = new AnnotationKey<>("stackframe");

	static CompilerOutput compile(Context context, AstNode node) {
		var main_class = new ClassWriter(0);
		main_class.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Main", null,
				"java/lang/Object", new String[] {});
		var method = main_class.visitMethod(Opcodes.ACC_PUBLIC +
				Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null,
				null);
		method.visitCode();
		method.visitMaxs(256, 256);

		var visitor = new CompilerVisitor(context, method);
		node.accept(visitor);

		method.visitInsn(Opcodes.RETURN);
		method.visitEnd();
		main_class.visitEnd();

		var classes = new ArrayList<CompiledClass>();

		var main_class_bytecode = main_class.toByteArray();
		var main_class_compiled = new CompiledClass("Main", main_class_bytecode);
		classes.add(main_class_compiled);

		for (var stackframe : context.getStackFrames())
			classes.add(compileStackFrame(stackframe));
		for (var reference : context.getReferences())
			classes.add(compileReference(context, reference));

		return new CompilerOutput(classes);
	}

	static CompiledClass compileStackFrame(StackFrame stackframe) {
		var frame = new ClassWriter(0);
		frame.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, stackframe.typename, null, "java/lang/Object", new String[] {});

		if (stackframe.parent.isEmpty())
			frame.visitField(Opcodes.ACC_PUBLIC, "parent", "Ljava/lang/Object;", null, null);
		else
			frame.visitField(Opcodes.ACC_PUBLIC, "parent", stackframe.parent.get().descriptor, null, null);
		for (var field : stackframe.fields)
			frame.visitField(Opcodes.ACC_PUBLIC, field.field, field.descriptor, null, null);

		var init = frame.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		init.visitCode();
		init.visitMaxs(1, 1);
		init.visitVarInsn(Opcodes.ALOAD, 0);
		init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		init.visitInsn(Opcodes.RETURN);
		init.visitEnd();

		frame.visitEnd();
		var frame_bytecode = frame.toByteArray();
		return new CompiledClass(stackframe.typename, frame_bytecode);
	}

	static CompiledClass compileReference(Context context, Reference reference) {
		var frame = new ClassWriter(0);
		frame.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, reference.typename, null, "java/lang/Object", new String[] {});
		frame.visitField(Opcodes.ACC_PUBLIC, "value", context.descriptorFromValueType(reference.target), null, null);

		var init = frame.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		init.visitCode();
		init.visitMaxs(1, 1);
		init.visitVarInsn(Opcodes.ALOAD, 0);
		init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		init.visitInsn(Opcodes.RETURN);
		init.visitEnd();

		frame.visitEnd();
		var frame_bytecode = frame.toByteArray();
		return new CompiledClass(reference.typename, frame_bytecode);
	}

	public static void printStackFrames(AstNode node) {
		var nameGenerator = new NameGenerator();
		var context = new Context(nameGenerator);
		var pipeline = Pipeline.begin(Pipeline.<AstNode>forward())
				.add(new TypeCheckStage())
				.add(new StackFrameStage(nameGenerator, context));
		pipeline.process(node);
		for (var stackframe : context.getStackFrames()) {
			System.out.println("--------------------------------------");
			System.out.println("Descriptor = " + stackframe.descriptor);
			System.out.println("Parent = " + stackframe.parent);
			System.out.println("Object = " + stackframe);
			for (var field : stackframe.fields)
				System.out.println("\t" + field.name + " -> " + field.field + " : " + field.descriptor);
			System.out.println(stackframe);
		}
	}
}
