package icl.ast;

public class AstField extends AstNode {

    public final AstNode value;
    public final String field;

    public AstField(AstNode value, String field) {
        this.value = value;
        this.field = field;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.acceptField(this);
    }

}
