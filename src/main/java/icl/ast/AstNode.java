package icl.ast;

public abstract class AstNode<T> {
	public final T annotation;

	public AstNode(T annotation) {
		this.annotation = annotation;
	}

	public abstract void accept(AstVisitor<T> visitor);
}
