package icl.stages.jvm2;

import icl.ast.AstNode;
import icl.pipeline.PipelineStage;

public class CompilerStage implements PipelineStage<AstNode, CompilerOutput> {

    @Override
    public CompilerOutput process(AstNode input) {
        var context = new Context();
        var environment = new Environment(context);
        context.emit(Compiler.main(environment, input));
        context.emit(Compiler.compile(environment));
        return new CompilerOutput(context.classes());
    }

}
