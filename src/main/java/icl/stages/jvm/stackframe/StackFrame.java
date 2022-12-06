package icl.stages.jvm.stackframe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import icl.ast.AstScope;
import icl.stages.jvm.NameGenerator;

public class StackFrame {
	// Class name for this stackframe
	public final String descriptor;

	// Parent stackframe
	public final Optional<StackFrame> parent;

	// Fields in this stackframe
	public final List<StackFrameField> fields;

	public StackFrame(String descriptor, Optional<StackFrame> parent, List<StackFrameField> fields) {
		this.descriptor = descriptor;
		this.parent = parent;
		this.fields = Collections.unmodifiableList(List.copyOf(fields));
	}

	public Optional<StackFrameField> getFieldByVarName(String name) {
		return this.fields.stream().filter(f -> f.name.equals(name)).findFirst();
	}

	public static StackFrame fromScope(NameGenerator nameGenerator, Optional<StackFrame> parent, AstScope scope) {
		var typename = nameGenerator.generateStackFrameName();
		var fieldVisitor = new FieldVisitor(nameGenerator);

		for (var stmt : scope.stmts)
			stmt.accept(fieldVisitor);
		scope.expr.accept(fieldVisitor);

		return new StackFrame(typename, parent, fieldVisitor.getFields());
	}
}