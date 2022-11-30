package icl.stages.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import icl.ValueType;
import icl.ast.*;

public class Parser {
	public static final AnnotationKey<Span> SPAN_KEY = new AnnotationKey<>("span");

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

	static AstCall astCall(Span span, AstNode function, List<AstNode> arguments) {
		var node = new AstCall(function, arguments);
		node.annotate(SPAN_KEY, span);
		return node;
	}

	static AstDecl astDecl(Span span, String name, AstNode value, boolean mutable) {
		var node = new AstDecl(name, value, mutable);
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
		var value = Short.parseShort(string);
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

	static AstStr astStr(Span span, String string) {
		var node = new AstStr(string);
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

	static AstPrint astPrint(Span span, AstNode expr) {
		var node = new AstPrint(expr);
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

	static ValueType valueTypeFromTokens(List<Token> tokens) {
		return ValueType.createVoid();
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
