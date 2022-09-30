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
	public double eval() {
		var v = this.expr.eval();
		return switch (this.kind) {
			case POS -> v;
			case NEG -> -v;
		};
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptUnaryOp(this);
	}

}
