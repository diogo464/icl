package icl.ast;

public class AstDecl implements AstNode {
	public final String name;
	public final AstNode value;
	public final Location location;

	public AstDecl(Token name, AstNode value) {
		this.name = name.image;
		this.value = value;
		this.location = new Location(name);
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptDecl(this);
	}

}
