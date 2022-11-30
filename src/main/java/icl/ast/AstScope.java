package icl.ast;

import java.util.List;

public class AstScope extends AstNode {
	public final List<AstNode> stmts;
	public final AstNode expr;

	public AstScope(List<AstNode> stmts, AstNode expr) {
		this.stmts = stmts;
		this.expr = expr;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptScope(this);
	}

}
