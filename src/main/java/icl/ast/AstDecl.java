package icl.ast;

public class AstDecl<T> extends AstNode<T> {
	public final String name;
	public final AstNode<T> value;
	public final boolean mutable;

	public AstDecl(T annotation, String name, AstNode<T> value, boolean mutable) {
		super(annotation);
		this.name = name;
		this.value = value;
		this.mutable = mutable;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptDecl(this);
	}

}
