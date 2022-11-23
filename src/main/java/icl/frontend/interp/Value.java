package icl.frontend.interp;

public abstract class Value {
	public static enum Type {
		Void,
		Number,
		Boolean,
	};

	private static class VoidValue extends Value {
		@Override
		public Type getType() {
			return Type.Void;
		}
	}

	private static class NumberValue extends Value {
		public final short value;

		public NumberValue(short value) {
			this.value = value;
		}

		@Override
		public Type getType() {
			return Type.Number;
		}

		public short getNumber() {
			return this.value;
		}
	}

	private static class BooleanValue extends Value {
		public final boolean value;

		public BooleanValue(boolean value) {
			this.value = value;
		}

		@Override
		public Type getType() {
			return Type.Boolean;
		}

		public boolean getBoolean() {
			return this.value;
		}
	}

	public abstract Type getType();

	public short getNumber() {
		throw new RuntimeException("Type is not a number");
	}

	public boolean getBoolean() {
		throw new RuntimeException("Type is not a boolean");
	}

	public static Value void_() {
		return new VoidValue();
	}

	public static Value number(short value) {
		return new NumberValue(value);
	}

	public static Value boolean_(boolean value) {
		return new BooleanValue(value);
	}
}
