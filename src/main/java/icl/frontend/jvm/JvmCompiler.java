package icl.frontend.jvm;

import java.util.List;

import icl.ast.AstNode;
import icl.mir.Mir;

public class JvmCompiler {
	public static class CompiledClass {
		public final String name;
		public final byte[] bytecode;

		public CompiledClass(String name, byte[] bytecode) {
			this.name = name;
			this.bytecode = bytecode;
		}
	}

	public static List<CompiledClass> compile(AstNode<Mir> node) {

		return null;
	}
}
