package icl.ast;

public class AstBinOp extends AstNode {
	public static enum Kind {
		ADD,
		SUB,
		MUL,
		DIV,
		IDIV,
		CMP,
		GT,
		GTE,
		LT,
		LTE,
		LAND,
		LOR,
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
