package icl.stages.print;

import java.io.IOException;
import java.io.OutputStream;

import icl.ast.AstBinOp;
import icl.ast.AstNode;
import icl.ast.AstUnaryOp;

public class PrettyPrinterStage implements icl.pipeline.PipelineStage<AstNode, String> {

	public PrettyPrinterStage() {
	}

	@Override
	public String process(AstNode input) {
		return printToString(input);
	}

	static void print(OutputStream stream, AstNode node) throws IOException {
		print(stream, node, false);
	}

	static void print(OutputStream stream, AstNode node, boolean print_annotations) throws IOException {
		var output = printToString(node, print_annotations);
		stream.write(output.getBytes());
	}

	static String printToString(AstNode node) {
		return printToString(node, false);
	}

	static String printToString(AstNode node, boolean print_annotations) {
		var visitor = new Visitor(print_annotations);
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
