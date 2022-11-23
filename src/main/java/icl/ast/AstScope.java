package icl.ast;

import java.util.List;

public class AstScope implements AstNode {
	public final List<AstNode> stmts;
	public final AstNode body;

	public AstScope(List<AstNode> decls, AstNode body) {
		this.stmts = decls;
		this.body = body;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptScope(this);
	}

}
