package icl.ast;

import java.util.List;

public class AstIf<T> extends AstNode<T> {
	public static class Conditional<T> {
		public final AstNode<T> condition;
		public final AstNode<T> expression;

		public Conditional(AstNode<T> condition, AstNode<T> expression) {
			this.condition = condition;
			this.expression = expression;
		}
	}

	public final List<Conditional<T>> conditionals;
	public final AstNode<T> fallthrough;

	public AstIf(T annotation, List<Conditional<T>> conditionals, AstNode<T> fallthrough) {
		super(annotation);
		if (conditionals == null || fallthrough == null)
			throw new IllegalArgumentException("conditionals and fallthrough must be non-null");
		this.conditionals = conditionals;
		this.fallthrough = fallthrough;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptIf(this);
	}

}
