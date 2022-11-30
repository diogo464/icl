package icl.ast;

import java.util.Map;

public class AstRecord extends AstNode {
    public final Map<String, AstNode> fields;

    public AstRecord(Map<String, AstNode> fields) {
        this.fields = fields;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.acceptRecord(this);
    }

}
