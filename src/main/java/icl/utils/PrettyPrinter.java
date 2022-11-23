package icl.utils;

import icl.ast.AstBinOp;
import icl.ast.AstDecl;
import icl.ast.AstScope;
import icl.ast.AstEmptyNode;
import icl.ast.AstNum;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;

public class PrettyPrinter implements AstVisitor {
	@Override
	public void acceptNum(AstNum node) {
		System.out.print(node.value);
	}

	@Override
	public void acceptBinOp(AstBinOp node) {
		node.left.accept(this);
		var symbol = switch (node.kind) {
			case ADD -> "+";
			case DIV -> "/";
			case MUL -> "*";
			case SUB -> "-";
			case LAND -> "&&";
			case CMP -> "==";
			case GT -> ">";
			case GTE -> ">=";
			case LT -> "<";
			case LTE -> "<=";
			case LOR -> "||";
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
			case DEREF -> "!";
			case LNOT -> "~";
		};
		System.out.print(symbol);
		node.expr.accept(this);
	}

	@Override
	public void acceptDecl(AstDecl node) {
		System.out.print(node.name);
		System.out.print(" = ");
		node.value.accept(this);
	}

	@Override
	public void acceptScope(AstScope node) {
		System.out.println("def");
		for (var decl : node.stmts) {
			decl.accept(this);
			System.out.println(";");
		}
		System.out.println("in");
		node.body.accept(this);
		System.out.println("");
		System.out.println("end");
	}

	@Override
	public void acceptEmptyNode(AstEmptyNode node) {
	}

	@Override
	public void acceptVar(AstVar node) {
		System.out.print(node.name);
	}
}
