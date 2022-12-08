package icl.ast;

public class AstNum extends AstNode {
	public final float value;

	public AstNum(short value) {
		this.value = value;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptNum(this);
	}
}
