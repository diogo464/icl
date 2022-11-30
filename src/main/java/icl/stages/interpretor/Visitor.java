package icl.stages.interpretor;

import icl.Environment;
import icl.ast.AstAssign;
import icl.ast.AstBinOp;
import icl.ast.AstBool;
import icl.ast.AstCall;
import icl.ast.AstDecl;
import icl.ast.AstScope;
import icl.ast.AstEmptyNode;
import icl.ast.AstFn;
import icl.ast.AstIf;
import icl.ast.AstLoop;
import icl.ast.AstNew;
import icl.ast.AstNum;
import icl.ast.AstPrint;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;
import icl.stages.typecheck.TypeCheckStage;

class Visitor implements AstVisitor {

	private final Environment<Value> environment;
	Value value;

	public Visitor(Environment<Value> environment) {
		this.environment = environment;
		this.value = null;
	}

	@Override
	public void acceptNum(AstNum node) {
		this.value = Value.createNumber(node.value);
	}

	@Override
	public void acceptBool(AstBool node) {
		this.value = Value.createBoolean(node.value);
	}

	@Override
	public void acceptBinOp(AstBinOp node) {
		var operand_type = node.left.getAnnotation(TypeCheckStage.TYPE_KEY);
		switch (operand_type.getKind()) {
			case Boolean -> {
				var left = InterpretorStage.interpret(this.environment, node.left).getBoolean();
				var right = InterpretorStage.interpret(this.environment, node.right).getBoolean();
				var value = switch (node.kind) {
					case CMP -> Value.createBoolean(left == right);
					case LAND -> Value.createBoolean(left && right);
					case LOR -> Value.createBoolean(left || right);
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			case Number -> {
				var left = InterpretorStage.interpret(this.environment, node.left).getNumber();
				var right = InterpretorStage.interpret(this.environment, node.right).getNumber();
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
	public void acceptUnaryOp(AstUnaryOp node) {
		var operand_type = node.expr.getAnnotation(TypeCheckStage.TYPE_KEY);
		switch (operand_type.getKind()) {
			case Number -> {
				var operand = InterpretorStage.interpret(this.environment, node.expr);
				var value = switch (node.kind) {
					case POS -> Value.createNumber(operand.getNumber());
					case NEG -> Value.createNumber(-operand.getNumber());
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			case Boolean -> {
				var operand = InterpretorStage.interpret(this.environment, node.expr);
				var value = switch (node.kind) {
					case LNOT -> Value.createBoolean(!operand.getBoolean());
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			case Reference -> {
				var operand = InterpretorStage.interpret(this.environment, node.expr);
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
	public void acceptDecl(AstDecl node) {
		var name = node.name;
		var value = InterpretorStage.interpret(this.environment, node.value);
		this.environment.define(name, value);
		this.value = Value.createVoid();
	}

	@Override
	public void acceptScope(AstScope node) {
		var env = this.environment.beginScope();
		for (var stmt : node.stmts) {
			InterpretorStage.interpret(env, stmt);
		}
		var value = InterpretorStage.interpret(env, node.expr);
		this.value = value;
	}

	@Override
	public void acceptEmptyNode(AstEmptyNode node) {
		this.value = Value.createVoid();
	}

	@Override
	public void acceptVar(AstVar node) {
		var value = this.environment.lookup(node.name);
		this.value = value;
	}

	@Override
	public void acceptCall(AstCall call) {
		var fnvalue = InterpretorStage.interpret(this.environment, call.function).getFunction();
		var callenv = fnvalue.env.beginScope();
		System.out.println("Argument count = " + call.arguments.size());
		for (var i = 0; i < call.arguments.size(); ++i) {
			var farg = fnvalue.args.get(i);
			var arg = call.arguments.get(i);
			var argvalue = InterpretorStage.interpret(this.environment, arg);
			System.out.println("Call define: " + farg.name);
			callenv.define(farg.name, argvalue);
		}
		var retvalue = InterpretorStage.interpret(callenv, fnvalue.body);
		this.value = retvalue;
	}

	@Override
	public void acceptIf(AstIf astIf) {
		for (var cond : astIf.conditionals) {
			var cond_value = InterpretorStage.interpret(this.environment, cond.condition).getBoolean();
			if (cond_value) {
				var value = InterpretorStage.interpret(this.environment, cond.expression);
				this.value = value;
				return;
			}
		}

		var value = InterpretorStage.interpret(this.environment, astIf.fallthrough);
		this.value = value;
	}

	@Override
	public void acceptLoop(AstLoop loop) {
		while (true) {
			var condition = InterpretorStage.interpret(this.environment, loop.condition);
			if (!condition.getBoolean())
				break;
			InterpretorStage.interpret(this.environment, loop.body);
		}
		this.value = Value.createVoid();
	}

	@Override
	public void acceptAssign(AstAssign assign) {
		var value = this.environment.lookup(assign.name);
		var new_value = InterpretorStage.interpret(this.environment, assign.value);
		value.assign(new_value);
	}

	@Override
	public void acceptPrint(AstPrint print) {
		var value = InterpretorStage.interpret(this.environment, print.expr);
		System.out.println(value);
	}

	@Override
	public void acceptNew(AstNew anew) {
		var value = InterpretorStage.interpret(this.environment, anew.value);
		var refvalue = Value.createReference(value);
		this.value = refvalue;
	}

	@Override
	public void acceptFn(AstFn fn) {
		var type = fn.getAnnotation(TypeCheckStage.TYPE_KEY);
		var env = this.environment.beginScope();
		var body = fn.body;
		var fnvalue = Value.createFunction(type, env, fn.arguments, body);
		this.value = fnvalue;
	}

}
