package icl.ast;

public class AstNum implements AstNode {
	private Token token;

	public AstNum(Token num) {
		this.token = num;
	}

	@Override
	public double eval() {
		return (double) Integer.parseInt(this.token.image);
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptNum(this);
	}
}
