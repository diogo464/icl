package icl.ast;

public class AstVar implements AstNode {
	public final Token name;

	public AstVar(Token name) {
		this.name = name;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptVar(this);
	}

}
