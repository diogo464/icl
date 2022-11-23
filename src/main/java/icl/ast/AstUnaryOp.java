package icl.ast;

public class AstUnaryOp<T> extends AstNode<T> {
	public static enum Kind {
		POS,
		NEG,
		LNOT,
		DEREF,
	}

	public final Kind kind;
	public final AstNode<T> expr;

	public AstUnaryOp(T annotation, Kind kind, AstNode<T> expr) {
		super(annotation);
		this.kind = kind;
		this.expr = expr;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptUnaryOp(this);
	}

}
