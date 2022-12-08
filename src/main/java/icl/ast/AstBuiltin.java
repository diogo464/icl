package icl.ast;

import java.util.List;

import icl.Builtin;

public class AstBuiltin extends AstNode {
    public final Builtin builtin;
    public final List<AstNode> args;

    public AstBuiltin(Builtin builtin, List<AstNode> args) {
        this.builtin = builtin;
        this.args = args;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.acceptBuiltin(this);
    }

}
