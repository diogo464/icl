package icl.frontend.print;

import icl.ast.AstAssign;
import icl.ast.AstBinOp;
import icl.ast.AstCall;
import icl.ast.AstDecl;
import icl.ast.AstEmptyNode;
import icl.ast.AstIf;
import icl.ast.AstLoop;
import icl.ast.AstNum;
import icl.ast.AstScope;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;

class Visitor<T> implements AstVisitor<T> {
	private final boolean print_annotations;
	private final StringBuilder builder;
	private int indent;

	public Visitor(boolean print_annotations) {
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

	private void printAnnotation(T annotation) {
		if (!this.print_annotations)
			return;
		this.print("[", annotation.toString(), "]");
	}

	@Override
	public void acceptNum(AstNum<T> node) {
		print("(");
		printAnnotation(node.annotation);
		print(String.valueOf(node.value));
		print(")");
	}

	@Override
	public void acceptBinOp(AstBinOp<T> node) {
		var op = PrettyPrinter.binOpKindToString(node.kind);
		print("(");
		printAnnotation(node.annotation);
		node.left.accept(this);
		print(" ", op, " ");
		node.right.accept(this);
		print(")");
	}

	@Override
	public void acceptUnaryOp(AstUnaryOp<T> node) {
		var op = PrettyPrinter.unaryOpKindToString(node.kind);
		printAnnotation(node.annotation);
		print(op);
		node.expr.accept(this);
	}

	@Override
	public void acceptDecl(AstDecl<T> node) {
		printAnnotation(node.annotation);
		print("let ");
		if (node.mutable)
			print("mut ");
		print(node.name, " = ");
		node.value.accept(this);
		print(";");
	}

	@Override
	public void acceptScope(AstScope<T> node) {
		this.indent += 1;
		printAnnotation(node.annotation);
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
	public void acceptEmptyNode(AstEmptyNode<T> node) {
	}

	@Override
	public void acceptVar(AstVar<T> node) {
		print("(");
		printAnnotation(node.annotation);
		print(node.name, ")");
	}

	@Override
	public void acceptCall(AstCall<T> call) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void acceptIf(AstIf<T> astIf) {
		printAnnotation(astIf.annotation);
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
	public void acceptLoop(AstLoop<T> loop) {
		printAnnotation(loop.annotation);
		print("while ");
		loop.condition.accept(this);
		println(" {");
		this.indent += 1;
		loop.body.accept(this);
		this.indent -= 1;
		println("}");
	}

	@Override
	public void acceptAssign(AstAssign<T> assign) {
		printAnnotation(assign.annotation);
		print(assign.name, " := ");
		assign.value.accept(this);
	}

}
