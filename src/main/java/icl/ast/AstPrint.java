package icl.ast;

public class AstPrint<T> extends AstNode<T> {
	public final AstNode<T> expr;

	public AstPrint(T annotation, AstNode<T> expr) {
		super(annotation);
		this.expr = expr;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptPrint(this);
	}
}
