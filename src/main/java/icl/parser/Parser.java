package icl.parser;

import java.io.InputStream;
import java.util.List;

import icl.ast.*;
import icl.hir.Hir;
import icl.hir.Location;

public class Parser {
	private Parser() {
	}

	public static AstNode<Hir> parse(InputStream source) throws ParseException {
		var parser = new CalcParser(source);
		var node = parser.Start();
		return node;
	}

	static Location locationFromToken(Token token) {
		return new Location(token.beginLine, token.endLine, token.beginColumn, token.endColumn);
	}

	static AstAssign<Hir> astAssign(Token name, AstNode<Hir> value) {
		return new AstAssign<>(annotationFromToken(name), name.image, value);
	}

	static AstBinOp<Hir> astBinOp(AstBinOp.Kind kind, AstNode<Hir> left, AstNode<Hir> right) {
		return new AstBinOp<>(left.annotation, kind, left, right);
	}

	static AstCall<Hir> astCall(AstNode<Hir> function, List<AstNode<Hir>> arguments) {
		return new AstCall<>(function.annotation, function, arguments);
	}

	static AstDecl<Hir> astDecl(Token name, AstNode<Hir> value, boolean mutable) {
		return new AstDecl<>(annotationFromToken(name), name.image, value, mutable);
	}

	static AstIf<Hir> astIf(List<AstIf.Conditional<Hir>> conditionals, AstNode<Hir> fallthrough) {
		return new AstIf<>(conditionals.get(0).condition.annotation, conditionals, fallthrough);
	}

	static AstIf.Conditional<Hir> astIfConditional(AstNode<Hir> condition, AstNode<Hir> expression) {
		return new AstIf.Conditional<>(condition, expression);
	}

	static AstLoop<Hir> astLoop(AstNode<Hir> condition, AstNode<Hir> body) {
		return new AstLoop<>(condition.annotation, condition, body);
	}

	static AstNum<Hir> astNum(Token token) {
		var annotation = annotationFromToken(token);
		var value = Short.parseShort(token.image);
		return new AstNum<>(annotation, value);
	}

	static AstBool<Hir> astBool(Token token) {
		var annotation = annotationFromToken(token);
		var value = Boolean.parseBoolean(token.image);
		return new AstBool<>(annotation, value);
	}

	static AstScope<Hir> astScope(List<AstNode<Hir>> stmts, AstNode<Hir> expr) {
		var annotation = expr.annotation;
		return new AstScope<>(annotation, stmts, expr);
	}

	static AstStr<Hir> astStr(Token token) {
		var annotation = annotationFromToken(token);
		var value = token.image;
		return new AstStr<>(annotation, value);
	}

	static AstUnaryOp<Hir> astUnaryOp(AstUnaryOp.Kind kind, AstNode<Hir> expr) {
		return new AstUnaryOp<>(expr.annotation, kind, expr);
	}

	static AstVar<Hir> astVar(Token token) {
		var annotation = annotationFromToken(token);
		return new AstVar<>(annotation, token.image);
	}

	static AstEmptyNode<Hir> astEmptyNode() {
		var annotation = new Hir(new Location());
		return new AstEmptyNode<>(annotation);
	}

	static AstPrint<Hir> astPrint(Token token, AstNode<Hir> expr) {
		var annotation = annotationFromToken(token);
		return new AstPrint<>(annotation, expr);
	}

	static AstNew<Hir> astNew(Token token, AstNode<Hir> value) {
		var annotation = annotationFromToken(token);
		return new AstNew<>(annotation, value);
	}

	private static Hir annotationFromToken(Token t) {
		return new Hir(locationFromToken(t));
	}
}
