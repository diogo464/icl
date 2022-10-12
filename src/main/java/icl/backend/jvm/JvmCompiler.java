package icl.backend.jvm;

import java.util.List;

public class JvmCompiler {
	public static class CompiledClass {
		public final String name;
		public final byte[] bytecode;

		public CompiledClass(String name, byte[] bytecode) {
			this.name = name;
			this.bytecode = bytecode;
		}
	}

	public static List<CompiledClass> compile(icl.ast.AstNode node) {
		var visitor = new Visitor();
		node.accept(visitor);
		return visitor.finish();
	}
}
