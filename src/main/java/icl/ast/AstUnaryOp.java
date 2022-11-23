package icl.ast;

public class AstUnaryOp implements AstNode {
	public enum Kind {
		POS,
		NEG,
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
