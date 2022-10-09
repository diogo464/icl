package icl.ast;

public class AstVar implements AstNode {
    public final Token name;

    public AstVar(Token name) {
        this.name = name;
    }

    @Override
    public double eval() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void accept(AstVisitor visitor) {
        // TODO Auto-generated method stub
        visitor.acceptVar(this);
    }

}
