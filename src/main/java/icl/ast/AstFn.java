package icl.ast;

import java.util.List;
import java.util.Optional;

import icl.type.ValueType;

public class AstFn<T> extends AstNode<T> {
	public static class Arg {
		public final String name;
		public final ValueType type;

		public Arg(String name, ValueType type) {
			this.name = name;
			this.type = type;
		}
	}

	public final List<Arg> arguments;
	public final Optional<ValueType> ret;
	public final AstNode<T> body;

	public AstFn(T annotation, List<Arg> arguments, Optional<ValueType> ret, AstNode<T> body) {
		super(annotation);
		this.arguments = arguments;
		this.ret = ret;
		this.body = body;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		visitor.acceptFn(this);
	}
}
