package icl.ast;

public class AstLoop implements AstNode {
	public final AstNode condition;
	public final AstNode body;

	public AstLoop(AstNode condition, AstNode body) {
		this.condition = condition;
		this.body = body;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptLoop(this);
	}

}
