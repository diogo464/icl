package icl.backend.interp;

import icl.ast.AstNode;

public class Interpretor {
    public static double interpret(AstNode node) {
        var visitor = new Visitor();
        node.accept(visitor);
        return visitor.value;
    }
}
