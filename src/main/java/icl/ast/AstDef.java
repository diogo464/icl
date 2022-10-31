package icl.ast;

import java.util.List;

public class AstDef implements AstNode {
	public final List<AstDecl> decls;
	public final AstNode body;

	public AstDef(List<AstDecl> decls, AstNode body) {
		this.decls = decls;
		this.body = body;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptDef(this);
	}

}
