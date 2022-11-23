package icl.ast;

public class AstEmptyNode<T> extends AstNode<T> {
	public AstEmptyNode(T annotation) {
		super(annotation);
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptEmptyNode(this);
	}

}
