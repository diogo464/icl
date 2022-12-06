package icl.stages.print;

import icl.ast.AstNode;
import icl.pipeline.PipelineStage;

public class NodePrinterStage implements PipelineStage<AstNode, Void> {

    public NodePrinterStage() {
    }

    @Override
    public Void process(AstNode input) {
        print(input);
        return null;
    }

    static void print(AstNode node) {
        var visitor = new NodeVisitor();
        node.accept(visitor);
    }

}
