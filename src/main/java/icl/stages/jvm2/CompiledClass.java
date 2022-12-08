package icl.stages.jvm2;

public class CompiledClass {
    public final String name;
    public final byte[] bytecode;

    public CompiledClass(String name, byte[] bytecode) {
        this.name = name;
        this.bytecode = bytecode;
    }
}
