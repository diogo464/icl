package icl.stages.jvm;

import java.util.List;

import icl.ast.AnnotationKey;
import icl.ast.AstNode;
import icl.pipeline.Pipeline;
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

public class Compiler {
	public static AnnotationKey<StackFrame> STACK_FRAME_KEY = new AnnotationKey<>("stackframe");

	public static void printStackFrames(AstNode node) {
		var nameGenerator = new NameGenerator();
		var context = new Context();
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

	static List<CompiledClass> compile(AstNode node) {

		return null;
	}
}
