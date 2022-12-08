package icl.ast;

public class AstNum extends AstNode {
	public final double value;

	public AstNum(double value) {
		this.value = value;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptNum(this);
	}
}
