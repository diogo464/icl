package icl.ast;

public interface AstVisitor<T> {
	void acceptNum(AstNum<T> node);

	void acceptBinOp(AstBinOp<T> node);

	void acceptUnaryOp(AstUnaryOp<T> node);

	void acceptDecl(AstDecl<T> node);

	void acceptScope(AstScope<T> node);

	void acceptEmptyNode(AstEmptyNode<T> node);

	void acceptVar(AstVar<T> node);

	void acceptCall(AstCall<T> call);

	void acceptIf(AstIf<T> astIf);

	void acceptLoop(AstLoop<T> loop);

	void acceptAssign(AstAssign<T> assign);
}
