package icl.ast;

public class AstStr implements AstNode {
	public final String value;
	public final Location location;

	public AstStr(Token token) {
		this.value = token.image;
		this.location = new Location(token);
	}

	@Override
	public void accept(AstVisitor visitor) {
		throw new RuntimeException("Trying to visit a string literal");
	}

}
