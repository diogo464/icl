
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import icl.ast.CalcParser;
import icl.ast.ParseException;
import icl.frontend.interp.Interpretor;
import icl.frontend.jvm.JvmCompiler;
import icl.utils.PrettyPrinter;

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
			var result = Interpretor.interpret(node);
			System.out.println("Result = " + result);
		}
	}

	private static InputStream getFileStream(String path) throws FileNotFoundException {
		InputStream source_stream = System.in;
		if (!path.equals("-"))
			source_stream = new FileInputStream(path);
		return source_stream;
	}
}
