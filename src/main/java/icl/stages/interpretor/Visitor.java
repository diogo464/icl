package icl.stages.interpretor;

import java.util.HashMap;

import icl.Environment;
import icl.ast.AstAssign;
import icl.ast.AstBinOp;
import icl.ast.AstBool;
import icl.ast.AstBuiltin;
import icl.ast.AstCall;
import icl.ast.AstDecl;
import icl.ast.AstScope;
import icl.ast.AstStr;
import icl.ast.AstTypeAlias;
import icl.ast.AstEmptyNode;
import icl.ast.AstField;
import icl.ast.AstFn;
import icl.ast.AstIf;
import icl.ast.AstLoop;
import icl.ast.AstNew;
import icl.ast.AstNum;
import icl.ast.AstPrint;
import icl.ast.AstRecord;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;
import icl.stages.interpretor.value.Value;
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
	public void acceptStr(AstStr str) {
		this.value = Value.createString(str.value);
	}

	@Override
	public void acceptBinOp(AstBinOp node) {
		var operand_type = node.left.getAnnotation(TypeCheckStage.TYPE_KEY);
		switch (operand_type.getKind()) {
			case Boolean -> {
				var left = InterpretorStage.interpret(this.environment, node.left).getBoolean().getValue();
				var right = InterpretorStage.interpret(this.environment, node.right).getBoolean().getValue();
				var value = switch (node.kind) {
					case CMP -> Value.createBoolean(left == right);
					case LAND -> Value.createBoolean(left && right);
					case LOR -> Value.createBoolean(left || right);
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			case Number -> {
				var left = InterpretorStage.interpret(this.environment, node.left).getNumber().getValue();
				var right = InterpretorStage.interpret(this.environment, node.right).getNumber().getValue();
				var value = switch (node.kind) {
					case ADD -> Value.createNumber(left + right);
					case SUB -> Value.createNumber(left - right);
					case MUL -> Value.createNumber(left * right);
					case DIV -> Value.createNumber(left / right);
					case IDIV -> Value.createNumber(Math.floor(left / right));
					case CMP -> Value.createBoolean(left == right);
					case GT -> Value.createBoolean(left > right);
					case GTE -> Value.createBoolean(left >= right);
					case LT -> Value.createBoolean(left < right);
					case LTE -> Value.createBoolean(left <= right);
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			case String -> {
				var left = InterpretorStage.interpret(this.environment, node.left).getString().getValue();
				var right = InterpretorStage.interpret(this.environment, node.right).getString().getValue();
				var value = switch (node.kind) {
					case ADD -> Value.createString(left + right);
					case CMP -> Value.createBoolean(left.equals(right));
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
					case POS -> Value.createNumber(operand.getNumber().getValue());
					case NEG -> Value.createNumber(-operand.getNumber().getValue());
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			case Boolean -> {
				var operand = InterpretorStage.interpret(this.environment, node.expr);
				var value = switch (node.kind) {
					case LNOT -> Value.createBoolean(!operand.getBoolean().getValue());
					default -> throw new IllegalStateException();
				};
				this.value = value;
			}
			case Reference -> {
				var operand = InterpretorStage.interpret(this.environment, node.expr);
				var value = switch (node.kind) {
					case DEREF -> operand.getReference().getValue();
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
		if (value == null)
			throw new RuntimeException("Variable " + node.name + " is not defined\n" + this.environment);
		this.value = value;
	}

	@Override
	public void acceptCall(AstCall call) {
		var fnvalue = InterpretorStage.interpret(this.environment, call.function).getFunction();
		var arguments = call.arguments.stream().map(arg -> InterpretorStage.interpret(this.environment, arg)).toList();
		this.value = fnvalue.evaluate(arguments);
	}

	@Override
	public void acceptIf(AstIf astIf) {
		for (var cond : astIf.conditionals) {
			var cond_value = InterpretorStage.interpret(this.environment, cond.condition).getBoolean().getValue();
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
			if (!condition.getBoolean().getValue())
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
		this.value = Value.createVoid();
	}

	@Override
	public void acceptPrint(AstPrint print) {
		var value = InterpretorStage.interpret(this.environment, print.expr);
		if (print.newline)
			System.out.println(value);
		else
			System.out.print(value);
		this.value = Value.createVoid();
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
		var fnenv = this.environment.beginScope();
		var body = fn.body;
		var fnvalue = Value.createFunction(type, fnenv, fn.arguments, body);
		fnenv.define("this", fnvalue);
		this.value = fnvalue;
	}

	@Override
	public void acceptRecord(AstRecord record) {
		var type = record.getAnnotation(TypeCheckStage.TYPE_KEY);
		var fields = new HashMap<String, Value>();
		for (var field : record.fields.entrySet()) {
			var value = InterpretorStage.interpret(this.environment, field.getValue());
			fields.put(field.getKey(), value);
		}
		this.value = Value.createRecord(type, fields);
	}

	@Override
	public void acceptField(AstField field) {
		var record = InterpretorStage.interpret(this.environment, field.value);
		var value = record.getRecord().getField(field.field);
		this.value = value;
	}

	@Override
	public void acceptTypeAlias(AstTypeAlias typeAlias) {
		this.value = Value.createVoid();
	}

	@Override
	public void acceptBuiltin(AstBuiltin builtin) {
		var args = builtin.args.stream().map(arg -> InterpretorStage.interpret(this.environment, arg)).toList();

		var result = switch (builtin.builtin) {
			case ABS -> {
				var x = args.get(0).getNumber().getValue();
				yield Value.createNumber(Math.abs(x));
			}
			case COS -> {
				var x = args.get(0).getNumber().getValue();
				yield Value.createNumber(Math.cos(x));
			}
			case MAX -> {
				var x = args.get(0).getNumber().getValue();
				var y = args.get(1).getNumber().getValue();
				yield Value.createNumber(Math.max(x, y));
			}
			case MIN -> {
				var x = args.get(0).getNumber().getValue();
				var y = args.get(1).getNumber().getValue();
				yield Value.createNumber(Math.min(x, y));
			}
			case PI -> {
				yield Value.createNumber(Math.PI);
			}
			case POW -> {
				var x = args.get(0).getNumber().getValue();
				var y = args.get(1).getNumber().getValue();
				yield Value.createNumber(Math.pow(x, y));
			}
			case SIN -> {
				var x = args.get(0).getNumber().getValue();
				yield Value.createNumber(Math.sin(x));
			}
			case SQRT -> {
				var x = args.get(0).getNumber().getValue();
				yield Value.createNumber(Math.sqrt(x));
			}
			case TAN -> {
				var x = args.get(0).getNumber().getValue();
				yield Value.createNumber(Math.tan(x));
			}
			case RAND -> {
				yield Value.createNumber(Math.random());
			}
		};
		this.value = result;
	}

}
