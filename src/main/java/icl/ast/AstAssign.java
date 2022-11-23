package icl.ast;

public class AstAssign implements AstNode {
	public final String name;
	public final AstNode value;
	public final Location location;

	public AstAssign(Token token, AstNode value) {
		this.name = token.image;
		this.value = value;
		this.location = new Location(token);
	}

	@Override
	public void accept(AstVisitor visitor) {
		// TODO: assignement visitor
	}
}
