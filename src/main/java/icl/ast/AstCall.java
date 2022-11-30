package icl.ast;

import java.util.List;

public class AstCall extends AstNode {
	public final AstNode function;
	public final List<AstNode> arguments;

	public AstCall(AstNode function, List<AstNode> arguments) {
		this.function = function;
		this.arguments = arguments;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptCall(this);
	}

}
