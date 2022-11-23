package icl.ast;

public class AstBinOp<T> extends AstNode<T> {
	public static enum Kind {
		ADD,
		SUB,
		MUL,
		DIV,
		CMP,
		GT,
		GTE,
		LT,
		LTE,
		LAND,
		LOR,
	}

	public final Kind kind;
	public final AstNode<T> left;
	public final AstNode<T> right;

	public AstBinOp(T annotation, Kind kind, AstNode<T> left, AstNode<T> right) {
		super(annotation);
		this.kind = kind;
		this.left = left;
		this.right = right;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptBinOp(this);
	}

}
