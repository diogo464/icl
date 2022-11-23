package icl.ast;

public class AstVar<T> extends AstNode<T> {
	public final String name;

	public AstVar(T annotation, String name) {
		super(annotation);
		this.name = name;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptVar(this);
	}

}
