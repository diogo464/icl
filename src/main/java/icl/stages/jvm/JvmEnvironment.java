package icl.stages.jvm;

import java.util.ArrayList;
import java.util.List;

import icl.ValueType;

class JvmEnvironment {
	public static class Variable {
		// Depth of the stack frame this value is at
		public final int depth;
		// Name of the field in the stack frame
		public final String name;
		// The stackframe this variable belongs to
		public final StackFrameOld stackframe;

		Variable(int depth, String name, StackFrameOld stackframe) {
			this.depth = depth;
			this.name = name;
			this.stackframe = stackframe;
		}
	}

	private final NameGenerator name_generator;
	private final List<StackFrameOld> stackframes;
	private icl.Environment<Variable> environment;
	private StackFrameOld current_frame;
	private int current_depth;

	public JvmEnvironment(NameGenerator name_generator) {
		this.name_generator = name_generator;
		this.stackframes = new ArrayList<>();
		this.environment = new icl.Environment<>();
		this.current_frame = null;
		this.current_depth = 0;
	}

	public int getCurrentDepth() {
		return this.current_depth;
	}

	public StackFrameOld getCurrentStackFrame() {
		return this.current_frame;
	}

	public void beginScope() {
		this.environment = this.environment.beginScope();
		var typename = this.name_generator.generateStackFrameName();
		var stackframe = new StackFrameOld(typename, this.current_frame);
		this.stackframes.add(stackframe);
		this.current_frame = stackframe;
		this.current_depth += 1;
	}

	public Variable define(String name, ValueType type) {
		var variable_name = this.name_generator.generateVariableName();
		this.current_frame.define(variable_name, null); // TODO: descriptor
		var variable = new Variable(this.current_depth, variable_name, this.current_frame);
		this.environment.define(name, variable);
		return variable;
	}

	public Variable lookup(String name) {
		return this.environment.lookup(name);
	}

	public void endScope() {
		if (this.current_depth == 0)
			throw new IllegalStateException("Cannot pop stackframe at depth 0");
		this.current_frame = this.current_frame.getParent();
		this.current_depth -= 1;
		this.environment = this.environment.endScope();
	}

	public List<StackFrameOld> getStackFrames() {
		return List.copyOf(this.stackframes);
	}
}
