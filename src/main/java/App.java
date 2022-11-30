
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import icl.pipeline.Pipeline;
import icl.stages.interpretor.InterpretorStage;
import icl.stages.interpretor.value.Value;
import icl.stages.parser.ParserStage;
import icl.stages.print.PrettyPrinterStage;
import icl.stages.typecheck.TypeCheckStage;

public class App {
	public static void main(String[] args) throws IOException {
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

	private static void commandCompile(String[] args) throws IOException {
		// var source_stream = getFileStream(args[0]);
		// var source_str = new String(source_stream.readAllBytes());
		// var source = source_str.getBytes();
		// System.out.println("SOURCE\n'" + source_str + "'");

		// var node = Parser.parse(new ByteArrayInputStream(source));
		// var compiled_classes = JvmCompiler.compile(Mir.toMir(node));

		// for (var compiled_class : compiled_classes) {
		// var class_name = compiled_class.name;
		// var class_bytes = compiled_class.bytecode;
		// var class_file = new FileOutputStream("calc_target/" + class_name +
		// ".class");
		// class_file.write(class_bytes);
		// class_file.close();
		// }
	}

	private static void commandPrint(String[] args) throws FileNotFoundException {
		var source_stream = getFileStream(args[0]);
		var pipeline = printPipeline();
		var output = pipeline.process(source_stream);
		System.out.print(output);
	}

	private static void commandRun(String[] args) throws FileNotFoundException {
		var source_stream = getFileStream(args[0]);
		var pipeline = interpretorPipeline();
		var value = pipeline.process(source_stream);
		System.out.println(value);
	}

	private static void commandInteractive() {
		var pipeline = interpretorPipeline();
		while (true) {
			var value = pipeline.process(System.in);
			System.out.println("Result = " + value);
		}
	}

	private static InputStream getFileStream(String path) throws FileNotFoundException {
		InputStream source_stream = System.in;
		if (!path.equals("-"))
			source_stream = new FileInputStream(path);
		return source_stream;
	}

	private static Pipeline<InputStream, Value> interpretorPipeline() {
		return Pipeline
				.begin(Pipeline.<InputStream>forward())
				.add(new ParserStage())
				.add(new TypeCheckStage())
				.add(new InterpretorStage());
	}

	private static Pipeline<InputStream, String> printPipeline() {
		return Pipeline
				.begin(Pipeline.<InputStream>forward())
				.add(new ParserStage())
				.add(new TypeCheckStage())
				.add(new PrettyPrinterStage());
	}
}
