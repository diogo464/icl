package icl.ast;

public class AstBinOp implements AstNode {
	public enum Kind {
		ADD,
		SUB,
		MUL,
		DIV,
	}

	public final Kind kind;
	public final AstNode left;
	public final AstNode right;

	public AstBinOp(Kind kind, AstNode left, AstNode right) {
		this.kind = kind;
		this.left = left;
		this.right = right;
	}

	@Override
	public double eval() {
		var l = this.left.eval();
		var r = this.right.eval();
		return switch (this.kind) {
			case ADD -> l + r;
			case SUB -> l - r;
			case MUL -> l * r;
			case DIV -> l / r;
		};
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptBinOp(this);
	}

}
