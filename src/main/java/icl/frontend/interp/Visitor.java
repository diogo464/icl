package icl.frontend.interp;

import icl.Environment;
import icl.ast.AstBinOp;
import icl.ast.AstDecl;
import icl.ast.AstScope;
import icl.ast.AstEmptyNode;
import icl.ast.AstNum;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;

class Visitor implements AstVisitor {
	private Environment<Value> env;
	private Value value;

	Visitor(Environment<Value> env) {
		this.env = env;
		this.value = null;
	}

	public Value getValue() {
		return this.value;
	}

	@Override
	public void acceptNum(AstNum node) {
		this.value = Value.number(node.value);
	}

	@Override
	public void acceptBinOp(AstBinOp node) {
		var leftVal = Interpretor.interpret(this.env, node.left);
		var rightVal = Interpretor.interpret(this.env, node.right);

		var left = leftVal.getNumber();
		var right = rightVal.getNumber();
		var value = switch (node.kind) {
			case ADD -> left + right;
			case SUB -> left - right;
			case MUL -> left * right;
			case DIV -> left / right;
			// TODO: implement others
			default -> throw new IllegalArgumentException("Unexpected value: " + node.kind);
		};

		this.value = Value.number((short) value);
	}

	@Override
	public void acceptUnaryOp(AstUnaryOp node) {
		node.expr.accept(this);
		var value = switch (node.kind) {
			case POS -> +this.value.getNumber();
			case NEG -> -this.value.getNumber();
			// TODO: Implement others
			default -> throw new IllegalArgumentException("Unexpected value: " + node.kind);
		};
		this.value = Value.number((short) value);
	}

	@Override
	public void acceptDecl(AstDecl node) {
		var tmp = this.value;
		node.value.accept(this);
		this.env.define(node.name, this.value);
		this.value = tmp;
	}

	@Override
	public void acceptScope(AstScope node) {
		var env = this.env.beginScope();
		for (var decl : node.stmts) {
			Interpretor.interpret(env, decl);
		}
		var value = Interpretor.interpret(env, node.body);
		this.value = value;
	}

	@Override
	public void acceptEmptyNode(AstEmptyNode node) {
		this.value = Value.void_();
	}

	@Override
	public void acceptVar(AstVar node) {
		var value = this.env.lookup(node.name);
		if (value == null)
			throw new RuntimeException("Variable " + node.name + " is not defined");
		this.value = value;
	}

}
