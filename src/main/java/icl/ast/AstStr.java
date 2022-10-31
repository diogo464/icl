package icl.ast;

public class AstStr implements AstNode {
	private Token token;

	public AstStr(Token str) {
		this.token = str;
	}

	@Override
	public void accept(AstVisitor visitor) {
		throw new RuntimeException("Trying to visit a string literal");
	}

}
