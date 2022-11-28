package icl.frontend.interp;

import java.util.List;

import icl.Environment;
import icl.ast.AstFn;
import icl.ast.AstNode;
import icl.mir.Mir;
import icl.type.ValueType;

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

	private static class RefValue extends Value {
		public Value value;

		public RefValue(ValueType type, Value value) {
			super(type);
			this.value = value;

			assert type.equals(ValueType.createReference(value.type));
		}

		public Value getReference() {
			return value;
		}

		public String toString() {
			return value.toString();
		}
	}

	public static class FnValue extends Value {
		public final Environment<Value> env;
		public final List<AstFn.Arg> args;
		public final AstNode<Mir> body;

		public FnValue(ValueType type, Environment<Value> environment, List<AstFn.Arg> args, AstNode<Mir> body) {
			super(type);
			this.env = environment;
			this.args = args;
			this.body = body;

			assert type.getKind() == ValueType.Kind.Function;
		}

		@Override
		public FnValue getFunction() {
			return this;
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
		throw new RuntimeException("Value is not a number, it is " + this.toString());
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

	public Value getReference() {
		throw new RuntimeException("Value is not a reference");
	}

	public void setReference(Value value) {
		throw new RuntimeException("Value is not a reference");
	}

	public FnValue getFunction() {
		throw new RuntimeException("Value is not a function, it is " + this.toString());
	}

	public void setFunction(FnValue value) {
		throw new RuntimeException("Value is not a function");
	}

	public void assign(Value other) {
		if (!this.getType().equals(other.getType()))
			throw new RuntimeException("Values are not the same type");

		var kind = this.getType().getKind();
		if (kind == ValueType.Kind.Number)
			this.setNumber(other.getNumber());
		else if (kind == ValueType.Kind.Boolean)
			this.setBoolean(other.getBoolean());
		else if (kind == ValueType.Kind.Reference)
			this.setReference(other.getReference());
		else if (kind == ValueType.Kind.Function)
			if (this.type.equals(other.type))
				this.setFunction(other.getFunction());
			else
				throw new RuntimeException("assignment function type missmatch");
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

	public static Value createReference(Value value) {
		return new RefValue(ValueType.createReference(value.type), value);
	}

	public static FnValue createFunction(
			ValueType type,
			Environment<Value> env,
			List<AstFn.Arg> args,
			AstNode<Mir> body) {
		return new FnValue(type, env, args, body);
	}
}
