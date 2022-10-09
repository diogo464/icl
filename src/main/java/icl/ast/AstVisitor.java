package icl.ast;

public interface AstVisitor {
	void acceptNum(AstNum node);

	void acceptBinOp(AstBinOp node);

	void acceptUnaryOp(AstUnaryOp node);

	void acceptDecl(AstDecl node);

	void acceptDef(AstDef node);

	void acceptEmptyNode(AstEmptyNode node);

	void acceptVar(AstVar node);
}
