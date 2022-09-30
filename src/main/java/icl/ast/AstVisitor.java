package icl.ast;

public interface AstVisitor {
	void acceptNum(AstNum node);

	void acceptBinOp(AstBinOp node);

	void acceptUnaryOp(AstUnaryOp node);
}
