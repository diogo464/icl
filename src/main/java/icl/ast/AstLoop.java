package icl.ast;

public class AstLoop<T> extends AstNode<T> {
	public final AstNode<T> condition;
	public final AstNode<T> body;

	public AstLoop(T annotation, AstNode<T> condition, AstNode<T> body) {
		super(annotation);
		this.condition = condition;
		this.body = body;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptLoop(this);
	}

}
