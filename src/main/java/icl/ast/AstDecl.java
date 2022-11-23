package icl.ast;

public class AstDecl implements AstNode {
	public final String name;
	public final AstNode value;
	public final Location location;
	public final boolean mutable;

	public AstDecl(Token name, AstNode value, boolean mutable) {
		this.name = name.image;
		this.value = value;
		this.location = new Location(name);
		this.mutable = mutable;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptDecl(this);
	}

}
