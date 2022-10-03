package icl;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import icl.ast.AstBinOp;
import icl.ast.AstNum;
import icl.ast.AstUnaryOp;
import icl.ast.AstVisitor;
import icl.ast.CalcParser;
import icl.ast.ParseException;

class AstPrintVisitor implements AstVisitor {
	@Override
	public void acceptNum(AstNum node) {
		System.out.print(node.eval());
	}

	@Override
	public void acceptBinOp(AstBinOp node) {
		node.left.accept(this);
		var symbol = switch (node.kind) {
			case ADD -> "+";
			case DIV -> "/";
			case MUL -> "*";
			case SUB -> "-";
		};
		System.out.print(" ");
		System.out.print(symbol);
		System.out.print(" ");
		node.right.accept(this);
	}

	@Override
	public void acceptUnaryOp(AstUnaryOp node) {
		var symbol = switch (node.kind) {
			case POS -> "+";
			case NEG -> "-";
		};
		System.out.print(symbol);
		node.expr.accept(this);
	}
}

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
}

public class App {
	public static void main(String[] args) throws ParseException, IOException {
		if (args.length > 0) {
			var filepath = args[0];
			InputStream source_stream = System.in;
			System.out.println("args[0] = '" + args[0] + "'");
			if (!filepath.equals("-"))
				source_stream = new FileInputStream(filepath);
			var bytecode = compile(source_stream);
			source_stream.close();
			var filewriter = new FileOutputStream("Main.class", false);
			filewriter.write(bytecode);
			filewriter.close();
		} else {
			var parser = new CalcParser(System.in);
			while (true) {
				var node = parser.Start();
				var result = node.eval();
				System.out.println("Result = " + result);
			}
		}
	}

	public static byte[] compile(InputStream stream) throws ParseException, IOException {
		var source_str = new String(stream.readAllBytes());
		var source = source_str.getBytes();
		System.out.println("SOURCE\n'" + source_str + "'");

		var parser = new CalcParser(new ByteArrayInputStream(source));
		var writer = new ClassWriter(0);
		var node = parser.Start();

		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Main", null, "java/lang/Object", new String[] {});
		var main = writer.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		main.visitCode();
		main.visitMaxs(256, 256);
		main.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

		var compiler = new AstBytecodeCompiler(main);
		node.accept(compiler);

		main.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false);
		main.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		main.visitInsn(Opcodes.RETURN);
		main.visitEnd();
		writer.visitEnd();

		return writer.toByteArray();
	}
}
