package icl.ast;

public class AstPrint extends AstNode {
	public final AstNode expr;

	public AstPrint(AstNode expr) {
		this.expr = expr;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptPrint(this);
	}
}
