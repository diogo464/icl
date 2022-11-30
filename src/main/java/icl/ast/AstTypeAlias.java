package icl.ast;

import icl.ValueType;

public class AstTypeAlias extends AstNode {

    public final String name;
    public final ValueType type;

    public AstTypeAlias(String name, ValueType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.acceptTypeAlias(this);
    }

}
