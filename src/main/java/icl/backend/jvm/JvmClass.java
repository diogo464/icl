package icl.backend.jvm;

public class JvmClass {
	public final String name;
	public final byte[] bytecode;

	JvmClass(String name, byte[] bytecode) {
		this.name = name;
		this.bytecode = bytecode;
	}
}
