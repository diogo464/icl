package icl.ast;

import java.util.List;

public class AstIf extends AstNode {
	public static class Conditional {
		public final AstNode condition;
		public final AstNode expression;

		public Conditional(AstNode condition, AstNode expression) {
			this.condition = condition;
			this.expression = expression;
		}
	}

	public final List<Conditional> conditionals;
	public final AstNode fallthrough;

	public AstIf(List<Conditional> conditionals, AstNode fallthrough) {
		if (conditionals == null || fallthrough == null)
			throw new IllegalArgumentException("conditionals and fallthrough must be non-null");
		this.conditionals = conditionals;
		this.fallthrough = fallthrough;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptIf(this);
	}

}
