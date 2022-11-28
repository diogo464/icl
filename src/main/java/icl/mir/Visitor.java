package icl.mir;

import icl.Environment;
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
import icl.hir.Hir;
import icl.type.ValueType;
import icl.utils.CalcUtils;

class Visitor implements AstVisitor<Hir> {
	private final Environment<ValueType> environment;
	public AstNode<Mir> lowered;

	public Visitor(Environment<ValueType> env) {
		this.environment = env;
		this.lowered = null;
	}

	@Override
	public void acceptNum(AstNum<Hir> node) {
		var annotation = new Mir(node.annotation, ValueType.createNumber());
		this.lowered = new AstNum<>(annotation, node.value);
	}

	@Override
	public void acceptBool(AstBool<Hir> node) {
		var annotation = new Mir(node.annotation, ValueType.createBoolean());
		this.lowered = new AstBool<>(annotation, node.value);
	}

	@Override
	public void acceptBinOp(AstBinOp<Hir> node) {
		var left = Mir.lower(this.environment, node.left);
		var right = Mir.lower(this.environment, node.right);
		if (!left.annotation.type.equals(right.annotation.type))
			throw new RuntimeException("Binary Op operands must have the same type, " + left.annotation.type + " and "
					+ right.annotation.type);

		var type = left.annotation.type;
		switch (type.getKind()) {
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
					type = ValueType.createBoolean();
				}
			}
			default -> throw new RuntimeException("BinaryOp operands must be boolean or number");
		}

		var annotation = new Mir(node.annotation, type);
		this.lowered = new AstBinOp<>(annotation, node.kind, left, right);
	}

	@Override
	public void acceptUnaryOp(AstUnaryOp<Hir> node) {
		var expr = Mir.lower(this.environment, node.expr);
		var kind = node.kind;
		var type = expr.annotation.type;
		switch (type.getKind()) {
			case Boolean -> {
				if (!CalcUtils.oneOf(kind, AstUnaryOp.Kind.LNOT))
					throw new RuntimeException("Boolean UnaryOp operator must be one of LNOT");
				var annotation = new Mir(node.annotation, type);
				this.lowered = new AstUnaryOp<>(annotation, kind, expr);
			}
			case Number -> {
				if (!CalcUtils.oneOf(kind, AstUnaryOp.Kind.POS, AstUnaryOp.Kind.NEG))
					throw new RuntimeException("Number UnaryOp operator must be one of POS, NEG");
				var annotation = new Mir(node.annotation, type);
				this.lowered = new AstUnaryOp<>(annotation, kind, expr);
			}
			case Reference -> {
				if (!CalcUtils.oneOf(kind, AstUnaryOp.Kind.DEREF))
					throw new RuntimeException("Reference UnaryOp operator must be one of DEREF");
				var annotation = new Mir(node.annotation, type.getReference().target);
				this.lowered = new AstUnaryOp<>(annotation, kind, expr);
			}
			default -> throw new RuntimeException("UnaryOp operand type must be Number, Boolean or Reference");
		}
	}

	@Override
	public void acceptDecl(AstDecl<Hir> node) {
		var value = Mir.lower(this.environment, node.value);
		var annotation = new Mir(node.annotation, ValueType.createVoid());
		this.environment.define(node.name, value.annotation.type);
		this.lowered = new AstDecl<>(annotation, node.name, value, node.mutable);
		System.out.println("Defining '" + node.name + "'");
		System.out.println(this.environment);
	}

	@Override
	public void acceptScope(AstScope<Hir> node) {
		var env = this.environment.beginScope();
		var stmts = node.stmts.stream().map(n -> Mir.lower(env, n)).toList();
		var expr = Mir.lower(env, node.expr);
		var annotation = new Mir(node.annotation, expr.annotation.type);
		this.lowered = new AstScope<>(annotation, stmts, expr);
	}

	@Override
	public void acceptEmptyNode(AstEmptyNode<Hir> node) {
		var annotation = new Mir(node.annotation, ValueType.createVoid());
		this.lowered = new AstEmptyNode<>(annotation);
	}

	@Override
	public void acceptVar(AstVar<Hir> node) {
		var type = this.environment.lookup(node.name);
		if (type == null)
			throw new RuntimeException(
					"Failed to lookup name to obtain type information: '" + node.name + "'\n" + this.environment);
		var annotation = new Mir(node.annotation, type);
		this.lowered = new AstVar<>(annotation, node.name);
	}

	@Override
	public void acceptCall(AstCall<Hir> call) {
		var function = Mir.lower(this.environment, call.function);
		var arguments = call.arguments.stream().map(n -> Mir.lower(this.environment, n)).toList();
		if (!function.annotation.type.isKind(ValueType.Kind.Function))
			throw new RuntimeException("Attempt to call non-function");

		var ftype = function.annotation.type.getFunction();
		if (arguments.size() != ftype.args.size())
			throw new RuntimeException("Function call argument count missmatch");

		for (var i = 0; i < ftype.args.size(); ++i) {
			var farg = ftype.args.get(i);
			var arg = arguments.get(i).annotation.type;
			if (farg.equals(arg))
				continue;

			throw new RuntimeException("Function call argument " + i + " missmatch: Expected " + farg.toString()
					+ " but got " + arg.toString());
		}

		assert ftype.ret != null;
		var annotation = new Mir(call.annotation, ftype.ret);
		this.lowered = new AstCall<>(annotation, function, arguments);
	}

	@Override
	public void acceptIf(AstIf<Hir> astIf) {
		var conditionals = astIf.conditionals.stream().map(c -> {
			var condition = Mir.lower(this.environment, c.condition);
			var expression = Mir.lower(this.environment, c.expression);
			return new AstIf.Conditional<>(condition, expression);
		}).toList();
		var fallthrough = Mir.lower(this.environment, astIf.fallthrough);

		for (var cond : conditionals)
			if (!cond.condition.annotation.type.isKind(ValueType.Kind.Boolean))
				throw new RuntimeException("If condition must be boolean");

		for (var cond : conditionals)
			if (!cond.expression.annotation.type.equals(fallthrough.annotation.type))
				throw new RuntimeException("All if branches must evaluate to the same type");

		var annotation = new Mir(astIf.annotation, fallthrough.annotation.type);
		this.lowered = new AstIf<>(annotation, conditionals, fallthrough);
	}

	@Override
	public void acceptLoop(AstLoop<Hir> loop) {
		var condition = Mir.lower(this.environment, loop.condition);
		var body = Mir.lower(this.environment, loop.body);

		if (!condition.annotation.type.isKind(ValueType.Kind.Boolean))
			throw new RuntimeException("Loop conditional must be boolean");

		var annotation = new Mir(loop.annotation, ValueType.createVoid());
		this.lowered = new AstLoop<>(annotation, condition, body);
	}

	@Override
	public void acceptAssign(AstAssign<Hir> assign) {
		var vartype = this.environment.lookup(assign.name);
		if (vartype == null)
			throw new RuntimeException("Failed to lookup variable: '" + assign.name + "'");
		var value = Mir.lower(this.environment, assign.value);
		if (!vartype.equals(value.annotation.type))
			throw new RuntimeException("Cant assign variable to value of different type");
		var annotation = new Mir(assign.annotation, ValueType.createVoid());
		this.lowered = new AstAssign<>(annotation, assign.name, value);
	}

	@Override
	public void acceptPrint(AstPrint<Hir> print) {
		System.out.println("acceptPrint Environment");
		System.out.println(this.environment);
		var expr = Mir.lower(this.environment, print.expr);
		var annotation = new Mir(print.annotation, ValueType.createVoid());
		this.lowered = new AstPrint<>(annotation, expr);
	}

	@Override
	public void acceptNew(AstNew<Hir> anew) {
		var value = Mir.lower(this.environment, anew.value);
		var reftype = ValueType.createReference(value.annotation.type);
		var annotation = new Mir(anew.annotation, reftype);
		this.lowered = new AstNew<>(annotation, value);
	}

	@Override
	public void acceptFn(AstFn<Hir> fn) {
		var bodyenv = this.environment.beginScope();
		for (var arg : fn.arguments)
			bodyenv.define(arg.name, arg.type);
		var body = Mir.lower(bodyenv, fn.body);
		if (fn.ret.isPresent())
			if (!fn.ret.get().equals(body.annotation.type))
				throw new RuntimeException("Function return type does not match body type");
		var argtypes = fn.arguments.stream().map(a -> a.type).toList();
		var type = ValueType.createFunction(argtypes, body.annotation.type);
		var annotation = new Mir(fn.annotation, type);
		this.lowered = new AstFn<>(annotation, fn.arguments, fn.ret, body);
	}

}
