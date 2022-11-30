package icl.stages.interpretor.value;

import java.util.List;
import java.util.Map;

import icl.Environment;
import icl.ValueType;
import icl.ast.AstFn;
import icl.ast.AstNode;

public abstract class Value {

	protected final ValueType type;

	protected Value(ValueType type) {
		this.type = type;
	}

	public final ValueType getType() {
		return this.type;
	}

	public final NumberValue getNumber() {
		if (this instanceof NumberValue)
			return ((NumberValue) this);
		throw new RuntimeException("Value is not a number");
	}

	public final BooleanValue getBoolean() {
		if (this instanceof BooleanValue)
			return ((BooleanValue) this);
		throw new RuntimeException("Value is not a boolean");
	}

	public final RefValue getReference() {
		if (this instanceof RefValue)
			return ((RefValue) this);
		throw new RuntimeException("Value is not a reference");
	}

	public final FnValue getFunction() {
		if (this instanceof FnValue)
			return ((FnValue) this);
		throw new RuntimeException("Value is not a function");
	}

	public final RecordValue getRecord() {
		if (this instanceof RecordValue)
			return ((RecordValue) this);
		throw new RuntimeException("Value is not a record");
	}

	public final void assign(Value other) {
		if ((this instanceof RefValue && !this.getReference().getTarget().equals(other.getType()))
				|| (!(this instanceof RefValue) && !this.getType().equals(other.getType())))
			throw new RuntimeException("Values are not the same type");

		if (this instanceof NumberValue) {
			((NumberValue) this).value = ((NumberValue) other).value;
		} else if (this instanceof BooleanValue) {
			((BooleanValue) this).value = ((BooleanValue) other).value;
		} else if (this instanceof RefValue) {
			((RefValue) this).value.assign(other);
		} else if (this instanceof FnValue) {
			((FnValue) this).env = ((FnValue) other).env;
			((FnValue) this).args = ((FnValue) other).args;
			((FnValue) this).body = ((FnValue) other).body;
		}
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
			AstNode body) {
		return new FnValue(type, env, args, body);
	}

	public static Value createRecord(ValueType type, Map<String, Value> fields) {
		return new RecordValue(type, fields);
	}
}
