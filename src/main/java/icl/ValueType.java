package icl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ValueType {
	public static enum Kind {
		Void,
		Boolean,
		Number,
		String,
		Reference,
		Function,
		Record,
		Alias
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
				if (comma)
					builder.append(",");
				builder.append(arg);
				comma = true;
			}
			builder.append("]]");
			return builder.toString();
		}
	};

	public static class Record {
		private final Map<String, ValueType> fields;

		private Record(Map<String, ValueType> fields) {
			this.fields = fields;
		}

		public ValueType get(String name) {
			var type = this.fields.get(name);
			if (type == null)
				throw new RuntimeException("No such field: " + name);
			return type;
		}

		public Optional<ValueType> tryGet(String name) {
			return Optional.ofNullable(this.fields.get(name));
		}

		public boolean contains(String name) {
			return this.fields.containsKey(name);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fields == null) ? 0 : fields.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Record other = (Record) obj;
			if (fields == null) {
				if (other.fields != null)
					return false;
			} else if (!fields.equals(other.fields))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Record [fields=" + fields + "]";
		}
	}

	private final Kind kind;
	private final Reference reference;
	private final Function function;
	private final Record record;
	private final String alias;

	private ValueType(Kind kind) {
		this.kind = kind;
		this.reference = null;
		this.function = null;
		this.record = null;
		this.alias = null;
	}

	private ValueType(Reference reference) {
		this.kind = Kind.Reference;
		this.reference = reference;
		this.function = null;
		this.record = null;
		this.alias = null;
	}

	private ValueType(Function function) {
		this.kind = Kind.Function;
		this.reference = null;
		this.function = function;
		this.record = null;
		this.alias = null;
	}

	private ValueType(Record record) {
		this.kind = Kind.Record;
		this.reference = null;
		this.function = null;
		this.record = record;
		this.alias = null;
	}

	private ValueType(String custom) {
		this.kind = Kind.Alias;
		this.reference = null;
		this.function = null;
		this.record = null;
		this.alias = custom;
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

	public Record getRecord() {
		if (!this.isKind(Kind.Record))
			throw new IllegalStateException("Called getRecord on ValueType that is not a reference");
		return this.record;
	}

	public String getAlias() {
		if (!this.isKind(Kind.Alias))
			throw new IllegalStateException("Called getAlias on ValueType that is not an alias");
		return this.alias;
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
		assert target != null;
		return new ValueType(new Reference(target));
	}

	public static ValueType createFunction(List<ValueType> args, ValueType ret) {
		assert args != null;
		assert ret != null;
		return new ValueType(new Function(args, ret));
	}

	public static ValueType createRecord(Map<String, ValueType> fields) {
		assert fields != null;
		return new ValueType(new Record(fields));
	}

	public static ValueType createAlias(String name) {
		assert name != null;
		return new ValueType(name);
	}

}
