package icl;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.OpenOption;

import org.objectweb.asm.ClassWriter;
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

public class App {
	public static void main(String[] args) throws ParseException, IOException {
		// var parser = new CalcParser(System.in);
		// var node = parser.Start();
		// node.accept(new AstPrintVisitor());
		// System.out.println();
		// System.out.println("Result = " + node.eval());

		var writer = new ClassWriter(0);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Main", null, "java/lang/Object", new String[] {});
		var main = writer.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		main.visitCode();
		main.visitMaxs(256, 256);
		main.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		main.visitIntInsn(Opcodes.SIPUSH, 20);
		main.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false);
		main.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		main.visitInsn(Opcodes.RETURN);
		main.visitEnd();
		writer.visitEnd();

		var bytecode = writer.toByteArray();
		var filewriter = new FileOutputStream("Main.class", false);
		filewriter.write(bytecode);
		filewriter.close();
	}
}
