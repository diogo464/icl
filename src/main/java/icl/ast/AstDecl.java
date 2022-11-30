package icl.ast;

public class AstDecl extends AstNode {
	public final String name;
	public final AstNode value;
	public final boolean mutable;

	public AstDecl(String name, AstNode value, boolean mutable) {
		this.name = name;
		this.value = value;
		this.mutable = mutable;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptDecl(this);
	}

}
