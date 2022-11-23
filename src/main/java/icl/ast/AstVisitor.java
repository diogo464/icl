package icl.ast;

public interface AstVisitor {
	void acceptNum(AstNum node);

	void acceptBinOp(AstBinOp node);

	void acceptUnaryOp(AstUnaryOp node);

	void acceptDecl(AstDecl node);

	void acceptScope(AstScope node);

	void acceptEmptyNode(AstEmptyNode node);

	void acceptVar(AstVar node);

	void acceptCall(AstCall call);

	void acceptIf(AstIf astIf);

	void acceptLoop(AstLoop loop);
}
