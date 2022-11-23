package icl.ast;

public class AstAssign<T> extends AstNode<T> {
	public final String name;
	public final AstNode<T> value;

	public AstAssign(T annotation, String name, AstNode<T> value) {
		super(annotation);
		this.name = name;
		this.value = value;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptAssign(this);
	}
}
