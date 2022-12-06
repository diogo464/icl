package icl.stages.jvm;

public class StackFrameField {
    // Field name
    public final String name;
    // JVM type descriptor
    public final String descriptor;

    public StackFrameField(String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        StackFrameField other = (StackFrameField) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (descriptor == null) {
            if (other.descriptor != null)
                return false;
        } else if (!descriptor.equals(other.descriptor))
            return false;
        return true;
    }
}
