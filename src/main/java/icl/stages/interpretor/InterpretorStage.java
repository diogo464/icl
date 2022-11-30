package icl.stages.interpretor;

import icl.Environment;
import icl.ast.AstNode;
import icl.pipeline.PipelineStage;

public class InterpretorStage implements PipelineStage<AstNode, Value> {

	@Override
	public Value process(AstNode input) {
		var env = new Environment<Value>();
		var output = interpret(env, input);
		return output;
	}

	static Value interpret(Environment<Value> env, AstNode node) {
		var visitor = new Visitor(env);
		node.accept(visitor);
		return visitor.value;
	}

}
