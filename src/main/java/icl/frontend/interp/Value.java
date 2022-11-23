package icl.frontend.interp;

import icl.mir.ValueType;

public abstract class Value {
	private static class VoidValue extends Value {
		public VoidValue(ValueType type) {
			super(type);
		}

		@Override
		public String toString() {
			return "void";
		}
	}

	private static class NumberValue extends Value {
		public short value;

		public NumberValue(ValueType type, short value) {
			super(type);
			this.value = value;
		}

		public short getNumber() {
			return this.value;
		}

		public void setNumber(short value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.valueOf(this.value);
		}
	}

	private static class BooleanValue extends Value {
		public boolean value;

		public BooleanValue(ValueType type, boolean value) {
			super(type);
			this.value = value;
		}

		public boolean getBoolean() {
			return this.value;
		}

		public void setBoolean(boolean value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.valueOf(this.value);
		}
	}

	private final ValueType type;

	private Value(ValueType type) {
		this.type = type;
	}

	public ValueType getType() {
		return this.type;
	}

	public short getNumber() {
		throw new RuntimeException("Value is not a number");
	}

	public void setNumber(short value) {
		throw new RuntimeException("Value is not a number");
	}

	public boolean getBoolean() {
		throw new RuntimeException("Value is not a boolean");
	}

	public void setBoolean(boolean value) {
		throw new RuntimeException("Value is not a boolean");
	}

	public void assign(Value other) {
		if (!this.getType().equals(other.getType()))
			throw new RuntimeException("Values are not the same type");
		if (this.getType().getKind().equals(ValueType.Kind.Number))
			this.setNumber(other.getNumber());
		else if (this.getType().getKind().equals(ValueType.Kind.Boolean))
			this.setBoolean(other.getBoolean());
	}

	public static Value createVoid() {
		return new VoidValue(ValueType.createVoid());
	}

	public static Value createNumber(int value) {
		return new NumberValue(ValueType.createNumber(), (short) value);
	}

	public static Value createNumber(short value) {
		return new NumberValue(ValueType.createNumber(), value);
	}

	public static Value createBoolean(boolean value) {
		return new BooleanValue(ValueType.createBoolean(), value);
	}
}
