package icl.ast;

public class AstDecl implements AstNode {
    public final Token name;
    public final AstNode value;

    public AstDecl(Token name, AstNode value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public double eval() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.acceptDecl(this);
    }

}
