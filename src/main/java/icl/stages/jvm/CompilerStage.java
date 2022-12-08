package icl.stages.jvm;

import icl.ast.AstNode;
import icl.pipeline.Pipeline;
import icl.pipeline.PipelineStage;
import icl.stages.jvm.stages.FunctionStage;
import icl.stages.jvm.stages.RecordStage;
import icl.stages.jvm.stages.StackFrameStage;

public class CompilerStage implements PipelineStage<AstNode, CompilerOutput> {

    @Override
    public CompilerOutput process(AstNode input) {
        var context = new Context();

        return Pipeline
                .begin(Pipeline.<AstNode>forward())
                .add(new StackFrameStage(context))
                .add(new RecordStage(context))
                .add(new FunctionStage(context))
                .add(Pipeline.function(main -> {
                    context.addCompiledClass(Compiler.main(context, main));
                    return new CompilerOutput(context.getCompiledClasses());
                }))
                .process(input);
    }

}
