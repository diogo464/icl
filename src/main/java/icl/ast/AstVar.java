package icl.ast;

public class AstVar extends AstNode {
	public final String name;

	public AstVar(String name) {
		this.name = name;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptVar(this);
	}

}
