package icl.ast;

public class AstNum implements AstNode {
	public final short value;
	public final Location location;

	public AstNum(Token num) {
		this.value = Short.parseShort(num.image);
		this.location = new Location(num);
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptNum(this);
	}
}
