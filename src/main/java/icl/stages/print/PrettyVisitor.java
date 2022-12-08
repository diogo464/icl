package icl.stages.print;

import icl.ast.AstAssign;
import icl.ast.AstBinOp;
import icl.ast.AstBool;
import icl.ast.AstCall;
import icl.ast.AstDecl;
import icl.ast.AstEmptyNode;
import icl.ast.AstField;
import icl.ast.AstFn;
import icl.ast.AstIf;
import icl.ast.AstLoop;
import icl.ast.AstNew;
import icl.ast.AstNum;
import icl.ast.AstPrint;
import icl.ast.AstRecord;
import icl.ast.AstScope;
import icl.ast.AstStr;
import icl.ast.AstTypeAlias;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;

class PrettyVisitor implements AstVisitor {
	private final boolean print_annotations;
	private final StringBuilder builder;
	private int indent;

	public PrettyVisitor(boolean print_annotations) {
		this.print_annotations = print_annotations;
		this.builder = new StringBuilder();
		this.indent = 0;
	}

	public String finish() {
		return this.builder.toString();
	}

	private void print(String... strings) {
		for (var string : strings)
			builder.append(string);
	}

	private void println(String... strings) {
		this.print(strings);
		this.builder.append("\n");
		for (var i = 0; i < this.indent; ++i)
			this.builder.append("\t");
	}

	@Override
	public void acceptNum(AstNum node) {
		print("(");
		print(String.valueOf(node.value));
		print(")");
	}

	@Override
	public void acceptBool(AstBool node) {
		print("(");
		print(String.valueOf(node.value));
		print(")");
	}

	@Override
	public void acceptStr(AstStr str) {
		print("(");
		print(str.value);
		print(")");
	}

	@Override
	public void acceptBinOp(AstBinOp node) {
		var op = PrintCommon.binOpKindToString(node.kind);
		print("(");
		node.left.accept(this);
		print(" ", op, " ");
		node.right.accept(this);
		print(")");
	}

	@Override
	public void acceptUnaryOp(AstUnaryOp node) {
		var op = PrintCommon.unaryOpKindToString(node.kind);
		print(op);
		node.expr.accept(this);
	}

	@Override
	public void acceptDecl(AstDecl node) {
		print("let ");
		if (node.mutable)
			print("mut ");
		print(node.name, " = ");
		node.value.accept(this);
		print(";");
	}

	@Override
	public void acceptScope(AstScope node) {
		this.indent += 1;
		println("{");
		var printLine = false;
		for (var stmt : node.stmts) {
			if (printLine)
				println();
			printLine = true;
			stmt.accept(this);
		}
		node.expr.accept(this);
		this.indent -= 1;
		println("}");
	}

	@Override
	public void acceptEmptyNode(AstEmptyNode node) {
	}

	@Override
	public void acceptVar(AstVar node) {
		print("(");
		print(node.name, ")");
	}

	@Override
	public void acceptCall(AstCall call) {
		call.function.accept(this);
		print("(");
		boolean printComma = false;
		for (var arg : call.arguments) {
			if (printComma)
				print(",");
			arg.accept(this);
			printComma = true;
		}
		print(")");
	}

	@Override
	public void acceptIf(AstIf astIf) {
		boolean isFirst = true;
		for (var cond : astIf.conditionals) {
			if (!isFirst)
				print("else ");
			print("if (");
			cond.condition.accept(this);
			print(") {");
			cond.expression.accept(this);
			print("}");
			isFirst = false;
		}
		print("else {");
		astIf.fallthrough.accept(this);
		print("}");
	}

	@Override
	public void acceptLoop(AstLoop loop) {
		print("while ");
		loop.condition.accept(this);
		println(" {");
		this.indent += 1;
		loop.body.accept(this);
		this.indent -= 1;
		println("}");
	}

	@Override
	public void acceptAssign(AstAssign assign) {
		print(assign.name, " := ");
		assign.value.accept(this);
	}

	@Override
	public void acceptPrint(AstPrint print) {
		print("print ");
		print.expr.accept(this);
	}

	@Override
	public void acceptNew(AstNew anew) {
		print("new ");
		anew.value.accept(this);
	}

	@Override
	public void acceptFn(AstFn fn) {
		print("fn(");
		for (var arg : fn.arguments)
			print(arg.name, ": ", arg.type.toString(), ",");
		print(")");
		if (fn.ret.isPresent())
			print(" -> ", fn.ret.get().toString());
		fn.body.accept(this);
	}

	@Override
	public void acceptRecord(AstRecord record) {
		print("{");
		boolean printComma = false;
		for (var field : record.fields.entrySet()) {
			if (printComma)
				print(",");
			print(field.getKey(), ": ");
			field.getValue().accept(this);
			printComma = true;
		}
		print("}");
	}

	@Override
	public void acceptField(AstField field) {
		field.value.accept(this);
		print(".", field.field);
	}

	@Override
	public void acceptTypeAlias(AstTypeAlias typeAlias) {
		print("type ", typeAlias.name, " = ", typeAlias.type.toString());
	}

}
