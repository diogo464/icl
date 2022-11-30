package icl.ast;

public interface AstVisitor {
	void acceptNum(AstNum node);

	void acceptBool(AstBool node);

	void acceptStr(AstStr node);

	void acceptBinOp(AstBinOp node);

	void acceptUnaryOp(AstUnaryOp node);

	void acceptDecl(AstDecl node);

	void acceptScope(AstScope node);

	void acceptEmptyNode(AstEmptyNode node);

	void acceptVar(AstVar node);

	void acceptCall(AstCall call);

	void acceptIf(AstIf astIf);

	void acceptLoop(AstLoop loop);

	void acceptAssign(AstAssign assign);

	void acceptPrint(AstPrint print);

	void acceptNew(AstNew anew);

	void acceptFn(AstFn fn);

	void acceptRecord(AstRecord record);

	void acceptField(AstField field);

	void acceptTypeAlias(AstTypeAlias typeAlias);
}
