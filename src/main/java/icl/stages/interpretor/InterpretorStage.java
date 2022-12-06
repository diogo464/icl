package icl.stages.interpretor;

import icl.Environment;
import icl.ast.AstNode;
import icl.pipeline.PipelineStage;
import icl.stages.interpretor.value.Value;

public class InterpretorStage implements PipelineStage<AstNode, Value> {

	@Override
	public Value process(AstNode input) {
		var env = new Environment<Value>();
		var output = interpret(env, input);
		return output;
	}

	public static Value interpret(Environment<Value> env, AstNode node) {
		var visitor = new Visitor(env);
		node.accept(visitor);
		assert visitor.value != null : "Evaluation of node " + node + " did not produce a value";
		return visitor.value;
	}

}
