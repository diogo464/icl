package icl.frontend.print;

import java.io.IOException;
import java.io.OutputStream;

import icl.ast.AstBinOp;
import icl.ast.AstNode;
import icl.ast.AstUnaryOp;

public class PrettyPrinter {

	public static <T> void print(OutputStream stream, AstNode<T> node) throws IOException {
		print(stream, node, false);
	}

	public static <T> void print(OutputStream stream, AstNode<T> node, boolean print_annotations) throws IOException {
		var output = printToString(node, print_annotations);
		stream.write(output.getBytes());
	}

	public static <T> String printToString(AstNode<T> node) {
		return printToString(node, false);
	}

	public static <T> String printToString(AstNode<T> node, boolean print_annotations) {
		var visitor = new Visitor<T>(print_annotations);
		node.accept(visitor);
		var output = visitor.finish();
		return output;
	}

	static String binOpKindToString(AstBinOp.Kind kind) {
		return switch (kind) {
			case ADD -> "+";
			case DIV -> "/";
			case MUL -> "*";
			case SUB -> "-";
			case LAND -> "&&";
			case CMP -> "==";
			case GT -> ">";
			case GTE -> ">=";
			case LT -> "<";
			case LTE -> "<=";
			case LOR -> "||";
		};
	}

	static String unaryOpKindToString(AstUnaryOp.Kind kind) {
		return switch (kind) {
			case POS -> "+";
			case NEG -> "-";
			case DEREF -> "!";
			case LNOT -> "~";
		};
	}
}
