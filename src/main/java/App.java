
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import icl.ast.AstBinOp;
import icl.ast.AstDecl;
import icl.ast.AstDef;
import icl.ast.AstEmptyNode;
import icl.ast.AstNum;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;
import icl.ast.CalcParser;
import icl.ast.ParseException;
import icl.backend.interp.Interpretor;
import icl.backend.jvm.JvmCompiler;
import icl.utils.PrettyPrinter;

class AstBytecodeCompiler implements AstVisitor {
	private final MethodVisitor visitor;

	public AstBytecodeCompiler(MethodVisitor visitor) {
		this.visitor = visitor;
	}

	@Override
	public void acceptNum(AstNum node) {
		var value = (int) node.eval();
		this.visitor.visitIntInsn(Opcodes.SIPUSH, value);
	}

	@Override
	public void acceptBinOp(AstBinOp node) {
		node.left.accept(this);
		node.right.accept(this);
		var opcode = switch (node.kind) {
			case ADD -> Opcodes.IADD;
			case DIV -> Opcodes.IDIV;
			case MUL -> Opcodes.IMUL;
			case SUB -> Opcodes.ISUB;
		};
		this.visitor.visitInsn(opcode);
	}

	@Override
	public void acceptUnaryOp(AstUnaryOp node) {
		switch (node.kind) {
			case POS -> {
				return;
			}
			case NEG -> {
				this.visitor.visitIntInsn(Opcodes.SIPUSH, -1);
				this.visitor.visitInsn(Opcodes.IMUL);
			}
		}
	}

	@Override
	public void acceptDef(AstDef node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptEmptyNode(AstEmptyNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptDecl(AstDecl node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptVar(AstVar node) {
		// TODO Auto-generated method stub

	}
}

public class App {
	public static void main(String[] args) throws ParseException, IOException {
		if (args.length == 0) {
			System.err.println("Usage:");
			System.err.println("compile <input file>");
			System.err.println("print <input file>");
			System.err.println("run <input file>");
			System.err.println("interactive");
			System.exit(1);
		}

		var command = args[0];
		var command_args = new String[args.length - 1];
		System.arraycopy(args, 1, command_args, 0, command_args.length);

		switch (command) {
			case "compile" -> commandCompile(command_args);
			case "print" -> commandPrint(command_args);
			case "run" -> commandRun(command_args);
			case "interactive" -> commandInteractive();
			default -> {
				System.err.println("Unknown command: " + command);
				System.exit(1);
			}
		}
	}

	private static void commandCompile(String[] args) throws ParseException, IOException {
		var source_stream = getFileStream(args[0]);

		var source_str = new String(source_stream.readAllBytes());
		var source = source_str.getBytes();
		System.out.println("SOURCE\n'" + source_str + "'");

		var parser = new CalcParser(new ByteArrayInputStream(source));
		var node = parser.Start();
		var compiled_classes = JvmCompiler.compile(node);

		for (var compiled_class : compiled_classes) {
			var class_name = compiled_class.name;
			var class_bytes = compiled_class.bytecode;
			var class_file = new FileOutputStream("calc_target/" + class_name + ".class");
			class_file.write(class_bytes);
			class_file.close();
		}
	}

	private static void commandPrint(String[] args) throws FileNotFoundException, ParseException {
		var source_stream = getFileStream(args[0]);
		var parser = new CalcParser(source_stream);
		var node = parser.Start();
		node.accept(new PrettyPrinter());
	}

	private static void commandRun(String[] args) throws FileNotFoundException, ParseException {
		var source_stream = getFileStream(args[0]);
		var parser = new CalcParser(source_stream);
		var node = parser.Start();
		var value = Interpretor.interpret(node);
		System.out.println(value);
	}

	private static void commandInteractive() throws ParseException {
		var parser = new CalcParser(System.in);
		while (true) {
			var node = parser.Start();
			var result = node.eval();
			System.out.println("Result = " + result);
		}
	}

	private static InputStream getFileStream(String path) throws FileNotFoundException {
		InputStream source_stream = System.in;
		if (!path.equals("-"))
			source_stream = new FileInputStream(path);
		return source_stream;
	}

	private static byte[] compile(InputStream stream) throws ParseException, IOException {
		return null;
		// var parser = new CalcParser(new ByteArrayInputStream(source));
		// var writer = new ClassWriter(0);
		// var node = parser.Start();

		// writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Main", null,
		// "java/lang/Object", new String[] {});
		// var main = writer.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
		// "main", "([Ljava/lang/String;)V", null,
		// null);
		// main.visitCode();
		// main.visitMaxs(256, 256);
		// main.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
		// "Ljava/io/PrintStream;");

		// var compiler = new AstBytecodeCompiler(main);
		// node.accept(compiler);

		// main.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf",
		// "(I)Ljava/lang/String;", false);
		// main.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println",
		// "(Ljava/lang/String;)V", false);
		// main.visitInsn(Opcodes.RETURN);
		// main.visitEnd();
		// writer.visitEnd();

		// return writer.toByteArray();
	}
}
