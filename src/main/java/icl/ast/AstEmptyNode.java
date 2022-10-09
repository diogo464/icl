package icl.ast;

public class AstEmptyNode implements AstNode {
    public AstEmptyNode() {
    }

    @Override
    public double eval() {
        return 0;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.acceptEmptyNode(this);
    }

}
