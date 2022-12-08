package icl.stages.jvm.struct;

import icl.ValueType;
import icl.stages.jvm.JvmUtils;

public class Reference {
    // JVM class name;
    public final String typename;

    // Type descriptor of the interface.
    // This is the JVM type descriptor for the stackframe class.
    public final String descriptor;

    public final ValueType target;

    public Reference(String typename, ValueType target) {
        this.typename = typename;
        this.descriptor = JvmUtils.descriptorFromTypename(typename);
        this.target = target;
    }

    @Override
    public String toString() {
        return "Reference [typename=" + typename + ", descriptor=" + descriptor + ", target=" + target + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((typename == null) ? 0 : typename.hashCode());
        result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
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
        Reference other = (Reference) obj;
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
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }
}
