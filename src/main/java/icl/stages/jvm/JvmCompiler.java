package icl.stages.jvm;

import java.util.List;

import icl.ast.AstNode;

public class JvmCompiler {
	public static class CompiledClass {
		public final String name;
		public final byte[] bytecode;

		public CompiledClass(String name, byte[] bytecode) {
			this.name = name;
			this.bytecode = bytecode;
		}
	}

	public static List<CompiledClass> compile(AstNode node) {

		return null;
	}
}
