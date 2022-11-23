package icl.frontend.interp;

import icl.Environment;
import icl.ast.AstNode;

public class Interpretor {
	public static double interpret(AstNode node) {
		var env = new Environment<Value>();
		var value = interpret(env, node);
		return value.getNumber();
	}

	static Value interpret(Environment<Value> env, AstNode node) {
		var visitor = new Visitor(env);
		node.accept(visitor);
		return visitor.getValue();
	}
}
