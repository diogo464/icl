package icl.ast;

public interface AstNode {
	void accept(AstVisitor visitor);
}
