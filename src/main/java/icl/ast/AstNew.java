package icl.ast;

public class AstNew<T> extends AstNode<T> {
	public final AstNode<T> value;

	public AstNew(T annotation, AstNode<T> value) {
		super(annotation);
		this.value = value;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptNew(this);
	}
}
