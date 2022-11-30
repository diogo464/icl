package icl.ast;

public class AstUnaryOp extends AstNode {
	public static enum Kind {
		POS,
		NEG,
		LNOT,
		DEREF,
	}

	public final Kind kind;
	public final AstNode expr;

	public AstUnaryOp(Kind kind, AstNode expr) {
		this.kind = kind;
		this.expr = expr;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptUnaryOp(this);
	}

}
