package icl.ast;

public class AstNew extends AstNode {
	public final AstNode value;

	public AstNew(AstNode value) {
		this.value = value;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptNew(this);
	}
}
