package icl.frontend.interp;

import icl.Environment;
import icl.ast.AstAssign;
import icl.ast.AstBinOp;
import icl.ast.AstBool;
import icl.ast.AstCall;
import icl.ast.AstDecl;
import icl.ast.AstScope;
import icl.ast.AstEmptyNode;
import icl.ast.AstIf;
import icl.ast.AstLoop;
import icl.ast.AstNew;
import icl.ast.AstNum;
import icl.ast.AstPrint;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;
import icl.mir.Mir;

class Visitor implements AstVisitor<Mir> {

	private final Environment<Value> environment;
	Value value;

	public Visitor(Environment<Value> environment) {
		this.environment = environment;
		this.value = null;
	}

	@Override
	public void acceptNum(AstNum<Mir> node) {
		this.value = Value.createNumber(node.value);
	}

	@Override
	public void acceptBool(AstBool<Mir> node) {
		this.value = Value.createBoolean(node.value);
	}

	@Override
	public void acceptBinOp(AstBinOp<Mir> node) {
		var operand_type = node.left.annotation.type;
		switch (operand_type.getKind()) {
			case Boolean -> {
				var left = Interpretor.interpret(this.environment, node.left).getBoolean();
				var right = Interpretor.interpret(this.environment, node.right).getBoolean();
				var value = switch (node.kind) {
					case CMP -> Value.createBoolean(left == right);
					case LAND -> Value.createBoolean(left && right);
					case LOR -> Value.createBoolean(left || right);
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			case Number -> {
				var left = Interpretor.interpret(this.environment, node.left).getNumber();
				var right = Interpretor.interpret(this.environment, node.right).getNumber();
				var value = switch (node.kind) {
					case ADD -> Value.createNumber(left + right);
					case SUB -> Value.createNumber(left - right);
					case MUL -> Value.createNumber(left * right);
					case DIV -> Value.createNumber(left / right);
					case CMP -> Value.createBoolean(left == right);
					case GT -> Value.createBoolean(left > right);
					case GTE -> Value.createBoolean(left >= right);
					case LT -> Value.createBoolean(left < right);
					case LTE -> Value.createBoolean(left <= right);
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			default -> throw new IllegalStateException();
		}
	}

	@Override
	public void acceptUnaryOp(AstUnaryOp<Mir> node) {
		var operand_type = node.expr.annotation.type;
		switch (operand_type.getKind()) {
			case Number -> {
				var operand = Interpretor.interpret(this.environment, node.expr);
				var value = switch (node.kind) {
					case POS -> Value.createNumber(operand.getNumber());
					case NEG -> Value.createNumber(-operand.getNumber());
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			case Boolean -> {
				var operand = Interpretor.interpret(this.environment, node.expr);
				var value = switch (node.kind) {
					case LNOT -> Value.createBoolean(!operand.getBoolean());
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			case Reference -> {
				var operand = Interpretor.interpret(this.environment, node.expr);
				var value = switch (node.kind) {
					case DEREF -> operand.getReference();
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			default -> throw new IllegalStateException();
		}
	}

	@Override
	public void acceptDecl(AstDecl<Mir> node) {
		var name = node.name;
		var value = Interpretor.interpret(this.environment, node.value);
		this.environment.define(name, value);
		this.value = Value.createVoid();
	}

	@Override
	public void acceptScope(AstScope<Mir> node) {
		var env = this.environment.beginScope();
		for (var stmt : node.stmts) {
			Interpretor.interpret(env, stmt);
		}
		var value = Interpretor.interpret(env, node.expr);
		this.value = value;
	}

	@Override
	public void acceptEmptyNode(AstEmptyNode<Mir> node) {
		this.value = Value.createVoid();
	}

	@Override
	public void acceptVar(AstVar<Mir> node) {
		var value = this.environment.lookup(node.name);
		this.value = value;
	}

	@Override
	public void acceptCall(AstCall<Mir> call) {
		// TODO: implement function call for interpretor
		throw new RuntimeException();
	}

	@Override
	public void acceptIf(AstIf<Mir> astIf) {
		for (var cond : astIf.conditionals) {
			var cond_value = Interpretor.interpret(this.environment, cond.condition).getBoolean();
			if (cond_value) {
				var value = Interpretor.interpret(this.environment, cond.expression);
				this.value = value;
				return;
			}
		}

		var value = Interpretor.interpret(this.environment, astIf.fallthrough);
		this.value = value;
	}

	@Override
	public void acceptLoop(AstLoop<Mir> loop) {
		while (true) {
			var condition = Interpretor.interpret(this.environment, loop.condition);
			if (!condition.getBoolean())
				break;
			Interpretor.interpret(this.environment, loop.body);
		}
		this.value = Value.createVoid();
	}

	@Override
	public void acceptAssign(AstAssign<Mir> assign) {
		var value = this.environment.lookup(assign.name);
		var new_value = Interpretor.interpret(this.environment, assign.value);
		value.assign(new_value);
	}

	@Override
	public void acceptPrint(AstPrint<Mir> print) {
		var value = Interpretor.interpret(this.environment, print.expr);
		System.out.println(value);
	}

	@Override
	public void acceptNew(AstNew<Mir> anew) {
		var value = Interpretor.interpret(this.environment, anew.value);
		var refvalue = Value.createReference(value);
		this.value = refvalue;
	}

}

// class Visitor implements AstVisitor {
// private Environment<Value> env;
// private Value value;

// Visitor(Environment<Value> env) {
// this.env = env;
// this.value = null;
// }

// public Value getValue() {
// return this.value;
// }

// @Override
// public void acceptNum(AstNum node) {
// this.value = Value.number(node.value);
// }

// @Override
// public void acceptBinOp(AstBinOp node) {
// var leftVal = Interpretor.interpret(this.env, node.left);
// var rightVal = Interpretor.interpret(this.env, node.right);

// var left = leftVal.getNumber();
// var right = rightVal.getNumber();
// var value = switch (node.kind) {
// case ADD -> left + right;
// case SUB -> left - right;
// case MUL -> left * right;
// case DIV -> left / right;
// // TODO: implement others
// default -> throw new IllegalArgumentException("Unexpected value: " +
// node.kind);
// };

// this.value = Value.number((short) value);
// }

// @Override
// public void acceptUnaryOp(AstUnaryOp node) {
// node.expr.accept(this);
// var value = switch (node.kind) {
// case POS -> +this.value.getNumber();
// case NEG -> -this.value.getNumber();
// // TODO: Implement others
// default -> throw new IllegalArgumentException("Unexpected value: " +
// node.kind);
// };
// this.value = Value.number((short) value);
// }

// @Override
// public void acceptDecl(AstDecl node) {
// var tmp = this.value;
// node.value.accept(this);
// this.env.define(node.name, this.value);
// this.value = tmp;
// }

// @Override
// public void acceptScope(AstScope node) {
// var env = this.env.beginScope();
// for (var decl : node.stmts) {
// Interpretor.interpret(env, decl);
// }
// var value = Interpretor.interpret(env, node.expr);
// this.value = value;
// }

// @Override
// public void acceptEmptyNode(AstEmptyNode node) {
// this.value = Value.void_();
// }

// @Override
// public void acceptVar(AstVar node) {
// var value = this.env.lookup(node.name);
// if (value == null)
// throw new RuntimeException("Variable " + node.name + " is not defined");
// this.value = value;
// }

// }
