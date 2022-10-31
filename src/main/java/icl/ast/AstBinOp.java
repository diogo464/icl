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
	public void accept(AstVisitor visitor) {
		visitor.acceptBinOp(this);
	}

}
