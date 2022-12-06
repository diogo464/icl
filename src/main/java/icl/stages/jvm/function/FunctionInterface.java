package icl.stages.jvm.function;

import icl.ValueType;
import icl.ValueType.Function;

public class FunctionInterface {
    // Function signature of this interface.
    public final ValueType.Function type;

    // Type descriptor of the interface.
    // This is the JVM type descriptor.
    public final String descriptor;

    public FunctionInterface(Function type, String descriptor) {
        this.type = type;
        this.descriptor = descriptor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        FunctionInterface other = (FunctionInterface) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (descriptor == null) {
            if (other.descriptor != null)
                return false;
        } else if (!descriptor.equals(other.descriptor))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FunctionInterface [type=" + type + ", descriptor=" + descriptor + "]";
    }
}
