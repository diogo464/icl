package icl.ast;

public class AstEmptyNode extends AstNode {
	public AstEmptyNode() {
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptEmptyNode(this);
	}

}
