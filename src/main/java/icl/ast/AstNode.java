package icl.ast;

public interface AstNode {
	double eval();

	void accept(AstVisitor visitor);
}
