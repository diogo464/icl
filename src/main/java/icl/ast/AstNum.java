package icl.ast;

public class AstNum<T> extends AstNode<T> {
	public final short value;

	public AstNum(T annotation, short value) {
		super(annotation);
		this.value = value;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptNum(this);
	}
}
