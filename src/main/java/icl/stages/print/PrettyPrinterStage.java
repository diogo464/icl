package icl.stages.print;

import java.io.IOException;
import java.io.OutputStream;

import icl.ast.AstNode;

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
		var visitor = new PrettyVisitor(print_annotations);
		node.accept(visitor);
		var output = visitor.finish();
		return output;
	}

}
