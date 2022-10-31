package icl.ast;

public class AstEmptyNode implements AstNode {
	public AstEmptyNode() {
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptEmptyNode(this);
	}

}
