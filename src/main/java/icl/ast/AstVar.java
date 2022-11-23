package icl.ast;

public class AstVar implements AstNode {
	public final String name;
	public final Location location;

	public AstVar(Token token) {
		this.name = token.image;
		this.location = new Location(token);
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptVar(this);
	}

}
