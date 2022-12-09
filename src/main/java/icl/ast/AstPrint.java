package icl.ast;

public class AstPrint extends AstNode {
	public final AstNode expr;
	public final boolean newline;
	public final boolean nodecimal;

	public AstPrint(AstNode expr, boolean newline, boolean nodecimal) {
		this.expr = expr;
		this.newline = newline;
		this.nodecimal = nodecimal;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptPrint(this);
	}
}
