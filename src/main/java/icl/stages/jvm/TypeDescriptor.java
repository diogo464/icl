package icl.stages.jvm;

public class TypeDescriptor {
    // The JVM class typename.
    public final String typename;

    // The JVM class type descriptor.
    public final String descriptor;

    public TypeDescriptor(String typename, String descriptor) {
        this.typename = typename;
        this.descriptor = descriptor;
    }
}
