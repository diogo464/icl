package icl.ast;

import java.util.List;

public class AstCall<T> extends AstNode<T> {
	public final AstNode<T> function;
	public final List<AstNode<T>> arguments;

	public AstCall(T annotation, AstNode<T> function, List<AstNode<T>> arguments) {
		super(annotation);
		this.function = function;
		this.arguments = arguments;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptCall(this);
	}

}
