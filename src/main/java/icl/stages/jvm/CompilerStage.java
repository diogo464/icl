package icl.stages.jvm;

import icl.ast.AstNode;
import icl.pipeline.Pipeline;
import icl.pipeline.PipelineStage;
import icl.stages.jvm.stackframe.StackFrameStage;

public class CompilerStage implements PipelineStage<AstNode, CompilerOutput> {

    @Override
    public CompilerOutput process(AstNode input) {
        var nameGenerator = new NameGenerator();
        var context = new Context(nameGenerator);
        return Pipeline
                .begin(new StackFrameStage(nameGenerator, context))
                .add(Pipeline.function(node -> Compiler.compile(context, node)))
                .process(input);
    }

}
