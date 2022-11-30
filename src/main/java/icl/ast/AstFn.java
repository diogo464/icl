package icl.ast;

import java.util.List;
import java.util.Optional;

import icl.type.ValueType;

public class AstFn extends AstNode {
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
	public final AstNode body;

	public AstFn(List<Arg> arguments, Optional<ValueType> ret, AstNode body) {
		this.arguments = arguments;
		this.ret = ret;
		this.body = body;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptFn(this);
	}
}
