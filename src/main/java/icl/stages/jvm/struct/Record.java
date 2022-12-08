package icl.stages.jvm.struct;

import icl.ValueType;

public class Record {
    // Record type of this record.
    public final ValueType.Record type;

    // The name of the class of this record.
    public final String typename;

    // The type of this record.
    // This is the JVM type descriptor of the record's class.
    public final String descriptor;

    public Record(ValueType.Record type, String typename, String descriptor) {
        this.type = type;
        this.typename = typename;
        this.descriptor = descriptor;
    }

    @Override
    public String toString() {
        return "Record [type=" + type + ", typename=" + typename + ", descriptor=" + descriptor + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((typename == null) ? 0 : typename.hashCode());
        result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
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
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (typename == null) {
            if (other.typename != null)
                return false;
        } else if (!typename.equals(other.typename))
            return false;
        if (descriptor == null) {
            if (other.descriptor != null)
                return false;
        } else if (!descriptor.equals(other.descriptor))
            return false;
        return true;
    }

}