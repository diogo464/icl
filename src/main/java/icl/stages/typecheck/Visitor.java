package icl.stages.typecheck;

import icl.Environment;
import icl.ValueType;
import icl.ast.AstAssign;
import icl.ast.AstBinOp;
import icl.ast.AstBool;
import icl.ast.AstCall;
import icl.ast.AstDecl;
import icl.ast.AstEmptyNode;
import icl.ast.AstFn;
import icl.ast.AstIf;
import icl.ast.AstLoop;
import icl.ast.AstNew;
import icl.ast.AstNode;
import icl.ast.AstNum;
import icl.ast.AstPrint;
import icl.ast.AstScope;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;
import icl.utils.CalcUtils;

class Visitor implements AstVisitor {
	private final Environment<ValueType> environment;
	public AstNode lowered;

	public Visitor(Environment<ValueType> env) {
		this.environment = env;
		this.lowered = null;
	}

	@Override
	public void acceptNum(AstNum node) {
		node.annotate(TypeCheckStage.TYPE_KEY, ValueType.createNumber());
	}

	@Override
	public void acceptBool(AstBool node) {
		node.annotate(TypeCheckStage.TYPE_KEY, ValueType.createBoolean());
	}

	@Override
	public void acceptBinOp(AstBinOp node) {
		var left = TypeCheckStage.check(this.environment, node.left);
		var right = TypeCheckStage.check(this.environment, node.right);
		var leftType = left.getAnnotation(TypeCheckStage.TYPE_KEY);
		var rightType = right.getAnnotation(TypeCheckStage.TYPE_KEY);

		if (!leftType.equals(rightType))
			throw new RuntimeException("Binary Op operands must have the same type, " + leftType + " and " + rightType);

		switch (leftType.getKind()) {
			case Boolean -> {
				if (!CalcUtils.oneOf(node.kind, AstBinOp.Kind.CMP, AstBinOp.Kind.LAND, AstBinOp.Kind.LOR)) {
					throw new RuntimeException("Invalid binary operator for boolean");
				}
			}
			case Number -> {
				if (!CalcUtils.oneOf(node.kind, AstBinOp.Kind.ADD, AstBinOp.Kind.SUB, AstBinOp.Kind.MUL,
						AstBinOp.Kind.DIV, AstBinOp.Kind.CMP, AstBinOp.Kind.GT, AstBinOp.Kind.GTE, AstBinOp.Kind.LT,
						AstBinOp.Kind.LTE)) {
					throw new RuntimeException("Invalid binary operator for number");
				}
				if (CalcUtils.oneOf(node.kind, AstBinOp.Kind.CMP, AstBinOp.Kind.GT, AstBinOp.Kind.GTE, AstBinOp.Kind.LT,
						AstBinOp.Kind.LTE)) {
					leftType = ValueType.createBoolean();
				}
			}
			default -> throw new RuntimeException("BinaryOp operands must be boolean or number");
		}

		node.annotate(TypeCheckStage.TYPE_KEY, leftType);
	}

	@Override
	public void acceptUnaryOp(AstUnaryOp node) {
		var expr = TypeCheckStage.check(this.environment, node.expr);
		var kind = node.kind;
		var type = expr.getAnnotation(TypeCheckStage.TYPE_KEY);
		switch (type.getKind()) {
			case Boolean -> {
				if (!CalcUtils.oneOf(kind, AstUnaryOp.Kind.LNOT))
					throw new RuntimeException("Boolean UnaryOp operator must be one of LNOT");
				node.annotate(TypeCheckStage.TYPE_KEY, type);
			}
			case Number -> {
				if (!CalcUtils.oneOf(kind, AstUnaryOp.Kind.POS, AstUnaryOp.Kind.NEG))
					throw new RuntimeException("Number UnaryOp operator must be one of POS, NEG");
				node.annotate(TypeCheckStage.TYPE_KEY, type);
			}
			case Reference -> {
				if (!CalcUtils.oneOf(kind, AstUnaryOp.Kind.DEREF))
					throw new RuntimeException("Reference UnaryOp operator must be one of DEREF");
				var derefType = type.getReference().target;
				node.annotate(TypeCheckStage.TYPE_KEY, derefType);
			}
			default -> throw new RuntimeException("UnaryOp operand type must be Number, Boolean or Reference");
		}
	}

	@Override
	public void acceptDecl(AstDecl node) {
		var value = TypeCheckStage.check(this.environment, node.value);
		this.environment.define(node.name, value.getAnnotation(TypeCheckStage.TYPE_KEY));
		node.annotate(TypeCheckStage.TYPE_KEY, ValueType.createVoid());
	}

	@Override
	public void acceptScope(AstScope node) {
		var env = this.environment.beginScope();
		for (var stmt : node.stmts)
			TypeCheckStage.check(env, stmt);
		var expr = TypeCheckStage.check(env, node.expr);
		node.annotate(TypeCheckStage.TYPE_KEY, expr.getAnnotation(TypeCheckStage.TYPE_KEY));
	}

	@Override
	public void acceptEmptyNode(AstEmptyNode node) {
		node.annotate(TypeCheckStage.TYPE_KEY, ValueType.createVoid());
	}

	@Override
	public void acceptVar(AstVar node) {
		var type = this.environment.lookup(node.name);
		if (type == null)
			throw new RuntimeException(
					"Failed to lookup name to obtain type information: '" + node.name + "'\n" + this.environment);
		node.annotate(TypeCheckStage.TYPE_KEY, type);
	}

	@Override
	public void acceptCall(AstCall call) {
		var function = TypeCheckStage.check(this.environment, call.function);
		var functionType = function.getAnnotation(TypeCheckStage.TYPE_KEY);

		var arguments = call.arguments.stream().map(n -> TypeCheckStage.check(this.environment, n)).toList();
		if (!functionType.isKind(ValueType.Kind.Function))
			throw new RuntimeException("Attempt to call non-function");

		var ftype = functionType.getFunction();
		if (arguments.size() != ftype.args.size())
			throw new RuntimeException("Function call argument count missmatch");

		for (var i = 0; i < ftype.args.size(); ++i) {
			var farg = ftype.args.get(i);
			var arg = arguments.get(i).getAnnotation(TypeCheckStage.TYPE_KEY);
			if (farg.equals(arg))
				continue;

			throw new RuntimeException("Function call argument " + i + " missmatch: Expected " + farg.toString()
					+ " but got " + arg.toString());
		}

		assert ftype.ret != null;
		call.annotate(TypeCheckStage.TYPE_KEY, ftype.ret);
	}

	@Override
	public void acceptIf(AstIf astIf) {
		var conditionals = astIf.conditionals.stream().map(c -> {
			var condition = TypeCheckStage.check(this.environment, c.condition);
			var expression = TypeCheckStage.check(this.environment, c.expression);
			return new AstIf.Conditional(condition, expression);
		}).toList();
		var fallthrough = TypeCheckStage.check(this.environment, astIf.fallthrough);

		for (var cond : conditionals)
			if (!cond.condition.getAnnotation(TypeCheckStage.TYPE_KEY).isKind(ValueType.Kind.Boolean))
				throw new RuntimeException("If condition must be boolean");

		for (var cond : conditionals)
			if (!cond.expression.getAnnotation(TypeCheckStage.TYPE_KEY)
					.equals(fallthrough.getAnnotation(TypeCheckStage.TYPE_KEY)))
				throw new RuntimeException("All if branches must evaluate to the same type");

		astIf.annotate(TypeCheckStage.TYPE_KEY, fallthrough.getAnnotation(TypeCheckStage.TYPE_KEY));
	}

	@Override
	public void acceptLoop(AstLoop loop) {
		var condition = TypeCheckStage.check(this.environment, loop.condition);
		TypeCheckStage.check(this.environment, loop.body);

		if (!condition.getAnnotation(TypeCheckStage.TYPE_KEY).isKind(ValueType.Kind.Boolean))
			throw new RuntimeException("Loop conditional must be boolean");

		loop.annotate(TypeCheckStage.TYPE_KEY, ValueType.createVoid());
	}

	@Override
	public void acceptAssign(AstAssign assign) {
		var vartype = this.environment.lookup(assign.name);
		if (vartype == null)
			throw new RuntimeException("Failed to lookup variable: '" + assign.name + "'");
		var value = TypeCheckStage.check(this.environment, assign.value);
		if (!vartype.equals(value.getAnnotation(TypeCheckStage.TYPE_KEY)))
			throw new RuntimeException("Cant assign variable to value of different type");
		assign.annotate(TypeCheckStage.TYPE_KEY, ValueType.createVoid());
	}

	@Override
	public void acceptPrint(AstPrint print) {
		System.out.println("acceptPrint Environment");
		System.out.println(this.environment);
		TypeCheckStage.check(this.environment, print.expr);
		print.annotate(TypeCheckStage.TYPE_KEY, ValueType.createVoid());
	}

	@Override
	public void acceptNew(AstNew anew) {
		var value = TypeCheckStage.check(this.environment, anew.value);
		var reftype = ValueType.createReference(value.getAnnotation(TypeCheckStage.TYPE_KEY));
		anew.annotate(TypeCheckStage.TYPE_KEY, reftype);
	}

	@Override
	public void acceptFn(AstFn fn) {
		var bodyenv = this.environment.beginScope();
		for (var arg : fn.arguments)
			bodyenv.define(arg.name, arg.type);
		var body = TypeCheckStage.check(bodyenv, fn.body);
		if (fn.ret.isPresent())
			if (!fn.ret.get().equals(body.getAnnotation(TypeCheckStage.TYPE_KEY)))
				throw new RuntimeException("Function return type does not match body type");
		var argtypes = fn.arguments.stream().map(a -> a.type).toList();
		var type = ValueType.createFunction(argtypes, body.getAnnotation(TypeCheckStage.TYPE_KEY));
		fn.annotate(TypeCheckStage.TYPE_KEY, type);
	}

}
