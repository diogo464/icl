package icl.ast;

public class AstBool extends AstNode {
	public final boolean value;

	public AstBool(boolean value) {
		this.value = value;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptBool(this);
	}
}
