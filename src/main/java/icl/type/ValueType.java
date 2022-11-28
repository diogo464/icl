package icl.type;

import java.util.List;
import java.util.Objects;

public class ValueType {
	public static enum Kind {
		Void,
		Boolean,
		Number,
		String,
		Reference,
		Function
	};

	public static class Reference {
		public final ValueType target;

		private Reference(ValueType target) {
			this.target = target;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null || !(other instanceof Reference))
				return false;
			var otherr = (Reference) other;
			return Objects.equals(this.target, otherr.target);
		}

		@Override
		public String toString() {
			return "Ref[" + this.target + "]";
		}
	};

	public static class Function {
		public final List<ValueType> args;
		public final ValueType ret;

		private Function(List<ValueType> args, ValueType ret) {
			this.ret = ret;
			this.args = args;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null || !(other instanceof Function))
				return false;
			var otherf = (Function) other;
			return Objects.equals(this.ret, otherf.ret) && Objects.equals(this.args, otherf.args);
		}

		@Override
		public String toString() {
			var builder = new StringBuilder();
			builder.append("Fun[");
			builder.append(this.ret);
			builder.append(",[");
			boolean comma = false;
			for (var arg : this.args) {
				builder.append(arg);
				if (comma)
					builder.append(",");
				comma = true;
			}
			builder.append("]]");
			return builder.toString();
		}
	};

	private final Kind kind;
	private final Reference reference;
	private final Function function;

	private ValueType(Kind kind) {
		this.kind = kind;
		this.reference = null;
		this.function = null;
	}

	private ValueType(Reference reference) {
		this.kind = Kind.Reference;
		this.reference = reference;
		this.function = null;
	}

	private ValueType(Function function) {
		this.kind = Kind.Function;
		this.reference = null;
		this.function = function;
	}

	public Kind getKind() {
		return this.kind;
	}

	public boolean isKind(Kind kind) {
		return this.kind.equals(kind);
	}

	public Reference getReference() {
		if (!this.isKind(Kind.Reference))
			throw new IllegalStateException("Called getReference on ValueType that is not a reference");
		return this.reference;
	}

	public Function getFunction() {
		if (!this.isKind(Kind.Function))
			throw new IllegalStateException("Called getFunction on ValueType that is not a reference");
		return this.function;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof ValueType))
			return false;
		var otherv = (ValueType) other;
		return Objects.equals(this.kind, otherv.kind) && Objects.equals(this.reference, otherv.reference)
				&& Objects.equals(this.function, otherv.function);
	}

	@Override
	public String toString() {
		if (this.reference != null)
			return this.reference.toString();
		if (this.function != null)
			return this.function.toString();
		return this.kind.toString();
	}

	public static ValueType createVoid() {
		return new ValueType(Kind.Void);
	}

	public static ValueType createBoolean() {
		return new ValueType(Kind.Boolean);
	}

	public static ValueType createNumber() {
		return new ValueType(Kind.Number);
	}

	public static ValueType createString() {
		return new ValueType(Kind.String);
	}

	public static ValueType createReference(ValueType target) {
		return new ValueType(new Reference(target));
	}

	public static ValueType createFunction(List<ValueType> args, ValueType ret) {
		return new ValueType(new Function(args, ret));
	}

	public static ValueType parse(String string) {
		if (string.equals("int"))
			return ValueType.createNumber();
		else if (string.equals("bool"))
			return ValueType.createBoolean();
		else if (string.equals("string"))
			return ValueType.createString();
		else if (string.startsWith("&"))
			return ValueType.createReference(parse(string.substring(1)));
		else
			throw new IllegalArgumentException("Invalid type: '" + string + "'");
	}
}
