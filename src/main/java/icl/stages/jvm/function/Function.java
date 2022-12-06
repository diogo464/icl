package icl.stages.jvm.function;

import icl.stages.jvm.stackframe.StackFrame;

public class Function {
    // Interface of this function.
    public final FunctionInterface iface;

    // The environment of this function.
    public final StackFrame environment;

    // Type descriptor of the function.
    // This is the JVM type descriptor of the function's class.
    public final String descriptor;

    public Function(FunctionInterface iface, StackFrame environment, String descriptor) {
        this.iface = iface;
        this.environment = environment;
        this.descriptor = descriptor;
    }

    @Override
    public String toString() {
        return "Function [iface=" + iface + ", environment=" + environment + ", descriptor=" + descriptor + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((iface == null) ? 0 : iface.hashCode());
        result = prime * result + ((environment == null) ? 0 : environment.hashCode());
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
        Function other = (Function) obj;
        if (iface == null) {
            if (other.iface != null)
                return false;
        } else if (!iface.equals(other.iface))
            return false;
        if (environment == null) {
            if (other.environment != null)
                return false;
        } else if (!environment.equals(other.environment))
            return false;
        if (descriptor == null) {
            if (other.descriptor != null)
                return false;
        } else if (!descriptor.equals(other.descriptor))
            return false;
        return true;
    }
}
