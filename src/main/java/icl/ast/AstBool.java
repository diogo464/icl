package icl.ast;

public class AstBool<T> extends AstNode<T> {
	public final boolean value;

	public AstBool(T annotation, boolean value) {
		super(annotation);
		this.value = value;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptBool(this);
	}
}
