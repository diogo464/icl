package icl.stages.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import icl.Builtin;
import icl.ValueType;
import icl.ast.*;
import icl.stages.parser.exception.ParserException;

public class Parser {

	public static final AnnotationKey<Span> SPAN_KEY = new AnnotationKey<>("span");

	record RecordFieldType(String name, ValueType type) {
	}

	record RecordField(String name, AstNode value) {
	}

	private Parser() {
	}

	public static AstNode parse(InputStream source) throws ParseException {
		var parser = new CalcParser(source);
		var node = parser.Start();
		return node;
	}

	static AstAssign astAssign(Span span, String name, AstNode value) {
		var node = new AstAssign(name, value);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstBinOp astBinOp(AstBinOp.Kind kind, AstNode left, AstNode right) {
		var node = new AstBinOp(kind, left, right);
		var span = span(left, right);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstCall astVarArgCall(Span span, AstNode function, List<AstNode> arguments) {
		if (arguments.size() < 2)
			throw new ParserException("Vararg call must have at least 2 arguments at " + span);
		if (arguments.size() == 2)
			return astCall(span, function, arguments);

		var args = new ArrayList<AstNode>();
		args.add(arguments.get(0));
		args.add(astVarArgCall(span, function, arguments.subList(1, arguments.size())));
		return astCall(span, function, args);
	};

	static AstCall astCall(Span span, AstNode function, List<AstNode> arguments) {
		var node = new AstCall(function, arguments);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstDecl astDecl(Span span, String name, AstNode value, boolean mutable, ValueType type) {
		var node = new AstDecl(name, value, mutable, Optional.ofNullable(type));
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstIf astIf(Span span, List<AstIf.Conditional> conditionals, AstNode fallthrough) {
		var node = new AstIf(conditionals, fallthrough);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstIf.Conditional astIfConditional(AstNode condition, AstNode expression) {
		return new AstIf.Conditional(condition, expression);
	}

	static AstLoop astLoop(Span span, AstNode condition, AstNode body) {
		var node = new AstLoop(condition, body);
		node.annotate(SPAN_KEY, span);
		return node;

	}

	static AstNum astNum(Span span, String string) {
		var value = Double.parseDouble(string);
		var node = new AstNum(value);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstBool astBool(Span span, String string) {
		var value = Boolean.parseBoolean(string);
		var node = new AstBool(value);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstScope astScope(Span span, List<AstNode> stmts, AstNode expr) {
		var node = new AstScope(stmts, expr);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstStr astStr(Span span, String image) {
		var node = new AstStr(image.substring(1, image.length() - 1));
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstUnaryOp astUnaryOp(AstUnaryOp.Kind kind, AstNode expr) {
		var node = new AstUnaryOp(kind, expr);
		var span = span(expr);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstVar astVar(Span span, String string) {
		var node = new AstVar(string);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstEmptyNode astEmptyNode(Span span) {
		var node = new AstEmptyNode();
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstPrint astPrint(Span span, AstNode expr, boolean newline, boolean nodecimal) {
		var node = new AstPrint(expr, newline, nodecimal);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstNew astNew(Span span, AstNode value) {
		var node = new AstNew(value);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstFn astFn(Span span, List<AstFn.Arg> arguments, ValueType ret, AstNode body) {
		if (arguments == null)
			arguments = new ArrayList<>();

		var node = new AstFn(arguments, Optional.ofNullable(ret), body);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstNode astRecord(Span span, List<RecordField> fields) {
		var map = new HashMap<String, AstNode>();
		for (var field : fields) {
			if (map.containsKey(field.name))
				throw new ParserException("Duplicate field name: " + field.name + " at " + span);
			map.put(field.name, field.value);
		}
		var node = new AstRecord(map);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstNode astField(Span span, AstNode value, String field) {
		var node = new AstField(value, field);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstNode astTypeAlias(Span span, String name, ValueType type) {
		var node = new AstTypeAlias(name, type);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstBuiltin astBuiltin(Span span, String name, List<AstNode> args) {
		var builtin = switch (name) {
			case "sin" -> Builtin.SIN;
			case "cos" -> Builtin.COS;
			case "tan" -> Builtin.TAN;
			case "sqrt" -> Builtin.SQRT;
			case "abs" -> Builtin.ABS;
			case "pow" -> Builtin.POW;
			case "max" -> Builtin.MAX;
			case "min" -> Builtin.MIN;
			case "rand" -> Builtin.RAND;
			case "pi" -> Builtin.PI;
			default -> throw new ParserException("Unknown builtin: " + name + " at " + span);
		};

		var node = new AstBuiltin(builtin, args);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static ValueType createRecordType(List<RecordFieldType> fields) {
		var map = new HashMap<String, ValueType>();
		for (var field : fields) {
			if (map.containsKey(field.name))
				throw new IllegalArgumentException("Duplicate field name: " + field.name);
			map.put(field.name, field.type);
		}
		return ValueType.createRecord(map);
	}

	static Span span(Token begin, Token end) {
		return new Span(begin.beginLine, begin.beginColumn, end.endLine, end.endColumn);
	}

	static Span span(AstNode begin, Token end) {
		return new Span(
				begin.getAnnotation(SPAN_KEY).startLine,
				begin.getAnnotation(SPAN_KEY).startColumn,
				end.endLine,
				end.endColumn);
	}

	static Span span(Token begin, AstNode end) {
		return new Span(
				begin.beginLine,
				begin.beginColumn,
				end.getAnnotation(SPAN_KEY).endLine,
				end.getAnnotation(SPAN_KEY).endColumn);
	}

	static Span span(AstNode begin, AstNode end) {
		return new Span(
				begin.getAnnotation(SPAN_KEY).startLine,
				begin.getAnnotation(SPAN_KEY).startColumn,
				end.getAnnotation(SPAN_KEY).endLine,
				end.getAnnotation(SPAN_KEY).endColumn);
	}

	static Span span(Token token) {
		return new Span(token.beginLine, token.beginColumn, token.endLine, token.endColumn);
	}

	static Span span(AstNode node) {
		return node.getAnnotation(SPAN_KEY);
	}
}
