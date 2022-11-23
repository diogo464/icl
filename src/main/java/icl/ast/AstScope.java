package icl.ast;

import java.util.List;

public class AstScope<T> extends AstNode<T> {
	public final List<AstNode<T>> stmts;
	public final AstNode<T> expr;

	public AstScope(T annotation, List<AstNode<T>> stmts, AstNode<T> expr) {
		super(annotation);
		this.stmts = stmts;
		this.expr = expr;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptScope(this);
	}

}
