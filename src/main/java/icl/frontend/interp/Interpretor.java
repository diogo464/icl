package icl.frontend.interp;

import icl.Environment;
import icl.ast.AstNode;
import icl.mir.Mir;

public class Interpretor {
	public static Value interpret(AstNode<Mir> node) {
		var env = new Environment<Value>();
		var value = interpret(env, node);
		return value;
	}

	static Value interpret(Environment<Value> env, AstNode<Mir> node) {
		var visitor = new Visitor(env);
		node.accept(visitor);
		return visitor.value;
	}
}
