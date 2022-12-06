package icl.stages.jvm;

import icl.ast.AstNode;
import icl.pipeline.PipelineStage;

public class CompilerStage implements PipelineStage<AstNode, CompilerOutput> {

    @Override
    public CompilerOutput process(AstNode input) {
        return null;
    }

}
