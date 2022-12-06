package icl.ast;

public class AstPrint extends AstNode {
	public final AstNode expr;
	public final boolean newline;

	public AstPrint(AstNode expr, boolean newline) {
		this.expr = expr;
		this.newline = newline;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptPrint(this);
	}
}
