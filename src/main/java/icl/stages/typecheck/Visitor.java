package icl.stages.typecheck;

import java.util.HashMap;

import icl.ValueType;
import icl.ast.AstAssign;
import icl.ast.AstBinOp;
import icl.ast.AstBool;
import icl.ast.AstBuiltin;
import icl.ast.AstCall;
import icl.ast.AstDecl;
import icl.ast.AstEmptyNode;
import icl.ast.AstField;
import icl.ast.AstFn;
import icl.ast.AstIf;
import icl.ast.AstLoop;
import icl.ast.AstNew;
import icl.ast.AstNum;
import icl.ast.AstPrint;
import icl.ast.AstRecord;
import icl.ast.AstScope;
import icl.ast.AstStr;
import icl.ast.AstTypeAlias;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;
import icl.stages.typecheck.exception.TypeCheckException;
import icl.utils.CalcUtils;

class Visitor implements AstVisitor {
	private final TypeCheckEnv env;

	public Visitor(TypeCheckEnv env) {
		this.env = env;
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
	public void acceptStr(AstStr str) {
		str.annotate(TypeCheckStage.TYPE_KEY, ValueType.createString());
	}

	@Override
	public void acceptBinOp(AstBinOp node) {
		var left = TypeCheckStage.check(this.env, node.left);
		var right = TypeCheckStage.check(this.env, node.right);
		var leftType = left.getAnnotation(TypeCheckStage.TYPE_KEY);
		var rightType = right.getAnnotation(TypeCheckStage.TYPE_KEY);

		if (!leftType.equals(rightType))
			throw new TypeCheckException(
					"Binary Op operands must have the same type, " + leftType + " and " + rightType, node);

		switch (leftType.getKind()) {
			case Boolean -> {
				if (!CalcUtils.oneOf(node.kind, AstBinOp.Kind.CMP, AstBinOp.Kind.LAND, AstBinOp.Kind.LOR)) {
					throw new TypeCheckException("Invalid binary operator for boolean", node);
				}
			}
			case Number -> {
				if (!CalcUtils.oneOf(node.kind, AstBinOp.Kind.ADD, AstBinOp.Kind.SUB, AstBinOp.Kind.MUL,
						AstBinOp.Kind.DIV, AstBinOp.Kind.CMP, AstBinOp.Kind.GT, AstBinOp.Kind.GTE, AstBinOp.Kind.LT,
						AstBinOp.Kind.LTE)) {
					throw new TypeCheckException("Invalid binary operator for number", node);
				}
				if (CalcUtils.oneOf(node.kind, AstBinOp.Kind.CMP, AstBinOp.Kind.GT, AstBinOp.Kind.GTE, AstBinOp.Kind.LT,
						AstBinOp.Kind.LTE)) {
					leftType = ValueType.createBoolean();
				}
			}
			case String -> {
				if (!CalcUtils.oneOf(node.kind, AstBinOp.Kind.ADD, AstBinOp.Kind.CMP)) {
					throw new TypeCheckException("Invalid binary operator for string", node);
				}

				switch (node.kind) {
					case CMP -> leftType = ValueType.createBoolean();
					case ADD -> leftType = ValueType.createString();
					default -> throw new TypeCheckException("Invalid binary operator for string", node);
				}
			}
			default -> throw new TypeCheckException("BinaryOp operands must be boolean or number", node);
		}

		node.annotate(TypeCheckStage.TYPE_KEY, leftType);
	}

	@Override
	public void acceptUnaryOp(AstUnaryOp node) {
		var expr = TypeCheckStage.check(this.env, node.expr);
		var kind = node.kind;
		var type = expr.getAnnotation(TypeCheckStage.TYPE_KEY);
		switch (type.getKind()) {
			case Boolean -> {
				if (!CalcUtils.oneOf(kind, AstUnaryOp.Kind.LNOT))
					throw new TypeCheckException("Boolean UnaryOp operator must be one of LNOT", node);
				node.annotate(TypeCheckStage.TYPE_KEY, type);
			}
			case Number -> {
				if (!CalcUtils.oneOf(kind, AstUnaryOp.Kind.POS, AstUnaryOp.Kind.NEG))
					throw new TypeCheckException("Number UnaryOp operator must be one of POS, NEG", node);
				node.annotate(TypeCheckStage.TYPE_KEY, type);
			}
			case Reference -> {
				if (!CalcUtils.oneOf(kind, AstUnaryOp.Kind.DEREF))
					throw new TypeCheckException("Reference UnaryOp operator must be one of DEREF", node);
				var derefType = type.getReference().target;
				node.annotate(TypeCheckStage.TYPE_KEY, derefType);
			}
			default -> throw new TypeCheckException("UnaryOp operand type must be Number, Boolean or Reference", node);
		}
	}

	@Override
	public void acceptDecl(AstDecl node) {
		var value = TypeCheckStage.check(this.env, node.value);
		var vtype = value.getAnnotation(TypeCheckStage.TYPE_KEY);
		if (node.type.isPresent()) {
			var declType = this.resolve(node.type.get());
			if (!declType.equals(vtype))
				throw new TypeCheckException(
						"Declared type and value type must be the same, got " + vtype + " expected " + declType,
						node);
		}
		this.env.value.define(node.name, new Variable(vtype, node.mutable));
		node.annotate(TypeCheckStage.TYPE_KEY, ValueType.createVoid());
	}

	@Override
	public void acceptScope(AstScope node) {
		var env = this.env.beginScope();
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
		var variable = this.env.value.lookup(node.name);
		if (variable == null)
			throw new TypeCheckException(
					"Failed to lookup name to obtain type information: '" + node.name + "'\n" + this.env, node);
		node.annotate(TypeCheckStage.TYPE_KEY, variable.type);
	}

	@Override
	public void acceptCall(AstCall call) {
		var function = TypeCheckStage.check(this.env, call.function);
		var functionType = function.getAnnotation(TypeCheckStage.TYPE_KEY);

		var arguments = call.arguments.stream().map(n -> TypeCheckStage.check(this.env, n)).toList();
		if (!functionType.isKind(ValueType.Kind.Function))
			throw new TypeCheckException("Attempt to call non-function", call);

		var ftype = functionType.getFunction();
		if (arguments.size() != ftype.args.size())
			throw new TypeCheckException("Function call argument count missmatch", call);

		for (var i = 0; i < ftype.args.size(); ++i) {
			var farg = ftype.args.get(i);
			var arg = arguments.get(i).getAnnotation(TypeCheckStage.TYPE_KEY);
			if (farg.equals(arg))
				continue;

			throw new TypeCheckException("Function call argument " + i + " missmatch: Expected " + farg.toString()
					+ " but got " + arg.toString(), call);
		}

		assert ftype.ret != null;
		call.annotate(TypeCheckStage.TYPE_KEY, ftype.ret);
	}

	@Override
	public void acceptIf(AstIf astIf) {
		var conditionals = astIf.conditionals.stream().map(c -> {
			var condition = TypeCheckStage.check(this.env, c.condition);
			var expression = TypeCheckStage.check(this.env, c.expression);
			return new AstIf.Conditional(condition, expression);
		}).toList();
		var fallthrough = TypeCheckStage.check(this.env, astIf.fallthrough);

		for (var cond : conditionals)
			if (!cond.condition.getAnnotation(TypeCheckStage.TYPE_KEY).isKind(ValueType.Kind.Boolean))
				throw new TypeCheckException("If condition must be boolean", astIf);

		for (var cond : conditionals)
			if (!cond.expression.getAnnotation(TypeCheckStage.TYPE_KEY)
					.equals(fallthrough.getAnnotation(TypeCheckStage.TYPE_KEY)))
				throw new TypeCheckException("All if branches must evaluate to the same type", astIf);

		astIf.annotate(TypeCheckStage.TYPE_KEY, fallthrough.getAnnotation(TypeCheckStage.TYPE_KEY));
	}

	@Override
	public void acceptLoop(AstLoop loop) {
		var condition = TypeCheckStage.check(this.env, loop.condition);
		TypeCheckStage.check(this.env, loop.body);

		if (!condition.getAnnotation(TypeCheckStage.TYPE_KEY).isKind(ValueType.Kind.Boolean))
			throw new TypeCheckException("Loop conditional must be boolean", loop);

		loop.annotate(TypeCheckStage.TYPE_KEY, ValueType.createVoid());
	}

	@Override
	public void acceptAssign(AstAssign assign) {
		var variable = this.env.value.lookup(assign.name);
		if (variable == null)
			throw new TypeCheckException("Failed to lookup variable: '" + assign.name + "'", assign);
		if (!variable.mutable)
			throw new TypeCheckException("Attempt to assign to immutable variable", assign);
		var vtype = variable.type;
		var value = TypeCheckStage.check(this.env, assign.value);
		if ((!vtype.isKind(ValueType.Kind.Reference) && !vtype.equals(value.getAnnotation(TypeCheckStage.TYPE_KEY)))
				|| (vtype.isKind(ValueType.Kind.Reference)
						&& !vtype.getReference().target.equals(value.getAnnotation(TypeCheckStage.TYPE_KEY))))
			throw new TypeCheckException("Cant assign variable to value of different type", assign);
		assign.annotate(TypeCheckStage.TYPE_KEY, ValueType.createVoid());
	}

	@Override
	public void acceptPrint(AstPrint print) {
		TypeCheckStage.check(this.env, print.expr);
		print.annotate(TypeCheckStage.TYPE_KEY, ValueType.createVoid());
	}

	@Override
	public void acceptNew(AstNew anew) {
		var value = TypeCheckStage.check(this.env, anew.value);
		var reftype = ValueType.createReference(value.getAnnotation(TypeCheckStage.TYPE_KEY));
		anew.annotate(TypeCheckStage.TYPE_KEY, reftype);
	}

	@Override
	public void acceptFn(AstFn fn) {
		var bodyenv = this.env.beginScope();
		for (var arg : fn.arguments)
			bodyenv.value.define(arg.name, new Variable(this.resolve(arg.type), true));
		var body = TypeCheckStage.check(bodyenv, fn.body);
		if (fn.ret.isPresent()) {
			var rettype = this.resolve(fn.ret.get());
			var bodytype = body.getAnnotation(TypeCheckStage.TYPE_KEY);
			if (!rettype.equals(bodytype)) {
				throw new TypeCheckException(
						"Function return type does not match body type, expected " + rettype.toString() + " but got "
								+ bodytype.toString(),
						fn);
			}
		}
		var argtypes = fn.arguments.stream().map(a -> this.resolve(a.type)).toList();
		var type = ValueType.createFunction(argtypes, body.getAnnotation(TypeCheckStage.TYPE_KEY));
		fn.annotate(TypeCheckStage.TYPE_KEY, type);
	}

	@Override
	public void acceptRecord(AstRecord record) {
		var typemap = new HashMap<String, ValueType>();
		for (var entry : record.fields.entrySet()) {
			var value = TypeCheckStage.check(this.env, entry.getValue());
			typemap.put(entry.getKey(), value.getAnnotation(TypeCheckStage.TYPE_KEY));
		}
		var type = ValueType.createRecord(typemap);
		record.annotate(TypeCheckStage.TYPE_KEY, type);
	}

	@Override
	public void acceptField(AstField field) {
		var record = TypeCheckStage.check(this.env, field.value);
		var rectype = record.getAnnotation(TypeCheckStage.TYPE_KEY);
		if (!rectype.isKind(ValueType.Kind.Record))
			throw new TypeCheckException("Attempt to access field on non-record type " + rectype, field);
		var fieldType = rectype.getRecord().tryGet(field.field);
		if (fieldType.isEmpty())
			throw new TypeCheckException("Attempt to access non-existent field", field);
		field.annotate(TypeCheckStage.TYPE_KEY, fieldType.get());
	}

	@Override
	public void acceptTypeAlias(AstTypeAlias typeAlias) {
		this.env.type.define(typeAlias.name, typeAlias.type);
		typeAlias.annotate(TypeCheckStage.TYPE_KEY, ValueType.createVoid());
	}

	private ValueType resolve(ValueType type) {
		if (type.isKind(ValueType.Kind.Reference)) {
			var target = type.getReference().target;
			return ValueType.createReference(this.resolve(target));
		} else if (type.isKind(ValueType.Kind.Function)) {
			var fn = type.getFunction();
			var args = fn.args.stream().map(this::resolve).toList();
			var ret = this.resolve(fn.ret);
			return ValueType.createFunction(args, ret);
		} else if (type.isKind(ValueType.Kind.Alias)) {
			var target = this.env.type.lookup(type.getAlias());
			if (target == null)
				throw new TypeCheckException("Failed to resolve type alias: " + type.getAlias());
			return this.resolve(target);
		} else if (type.isKind(ValueType.Kind.Record)) {
			var fields = type.getRecord().fields();
			var newfields = new HashMap<String, ValueType>();
			for (var entry : fields) {
				newfields.put(entry.getKey(), this.resolve(entry.getValue()));
			}
			return ValueType.createRecord(newfields);
		}
		return type;
	}

	@Override
	public void acceptBuiltin(AstBuiltin builtin) {
		builtin.args.forEach(n -> n.accept(this));
		var argc = builtin.args.size();
		var types = builtin.args.stream().map(n -> n.getAnnotation(TypeCheckStage.TYPE_KEY));

		var type = switch (builtin.builtin) {
			case ABS -> {
				if (argc != 1)
					throw new TypeCheckException("ABS takes exactly 1 argument", builtin);

				var tvalid = types.allMatch(t -> t.isKind(ValueType.Kind.Number));
				if (!tvalid)
					throw new TypeCheckException("ABS takes only number arguments", builtin);

				yield ValueType.createNumber();
			}
			case COS -> {
				if (argc != 1)
					throw new TypeCheckException("COS takes exactly 1 argument", builtin);

				var tvalid = types.allMatch(t -> t.isKind(ValueType.Kind.Number));
				if (!tvalid)
					throw new TypeCheckException("COS takes only number arguments", builtin);

				yield ValueType.createNumber();
			}
			case MAX -> {
				if (argc != 2)
					throw new TypeCheckException("MAX takes exactly 2 arguments", builtin);

				var tvalid = types.allMatch(t -> t.isKind(ValueType.Kind.Number));
				if (!tvalid)
					throw new TypeCheckException("MAX takes only number arguments", builtin);

				yield ValueType.createNumber();
			}
			case MIN -> {
				if (argc != 2)
					throw new TypeCheckException("MIN takes exactly 2 arguments", builtin);

				var tvalid = types.allMatch(t -> t.isKind(ValueType.Kind.Number));
				if (!tvalid)
					throw new TypeCheckException("MIN takes only number arguments", builtin);

				yield ValueType.createNumber();
			}
			case PI -> {
				if (argc != 0)
					throw new TypeCheckException("PI takes exactly 0 arguments", builtin);
				yield ValueType.createNumber();
			}
			case POW -> {
				if (argc != 2)
					throw new TypeCheckException("POW takes exactly 2 arguments", builtin);

				var tvalid = types.allMatch(t -> t.isKind(ValueType.Kind.Number));
				if (!tvalid)
					throw new TypeCheckException("POW takes only number arguments", builtin);

				yield ValueType.createNumber();
			}
			case SIN -> {
				if (argc != 1)
					throw new TypeCheckException("SIN takes exactly 1 argument", builtin);

				var tvalid = types.allMatch(t -> t.isKind(ValueType.Kind.Number));
				if (!tvalid)
					throw new TypeCheckException("SIN takes only number arguments", builtin);

				yield ValueType.createNumber();
			}
			case SQRT -> {
				if (argc != 1)
					throw new TypeCheckException("SQRT takes exactly 1 argument", builtin);

				var tvalid = types.allMatch(t -> t.isKind(ValueType.Kind.Number));
				if (!tvalid)
					throw new TypeCheckException("SQRT takes only number arguments", builtin);

				yield ValueType.createNumber();
			}
			case TAN -> {
				if (argc != 1)
					throw new TypeCheckException("TAN takes exactly 1 argument", builtin);

				var tvalid = types.allMatch(t -> t.isKind(ValueType.Kind.Number));
				if (!tvalid)
					throw new TypeCheckException("TAN takes only number arguments", builtin);

				yield ValueType.createNumber();
			}
		};

		builtin.annotate(TypeCheckStage.TYPE_KEY, type);
	}

}
