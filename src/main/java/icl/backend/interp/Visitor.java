package icl.backend.interp;

import icl.Environment;
import icl.ast.AstBinOp;
import icl.ast.AstDecl;
import icl.ast.AstDef;
import icl.ast.AstEmptyNode;
import icl.ast.AstNum;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;

class Visitor implements AstVisitor {
    private Environment<Double> env;
    double value;

    Visitor() {
        this.env = new Environment<>();
        this.value = 0.0;
    }

    @Override
    public void acceptNum(AstNum node) {
        this.value = node.value();
    }

    @Override
    public void acceptBinOp(AstBinOp node) {
        node.left.accept(this);
        var left = this.value;
        node.right.accept(this);
        var right = this.value;
        this.value = switch (node.kind) {
            case ADD -> left + right;
            case SUB -> left - right;
            case MUL -> left * right;
            case DIV -> left / right;
        };
    }

    @Override
    public void acceptUnaryOp(AstUnaryOp node) {
        node.expr.accept(this);
        this.value = switch (node.kind) {
            case POS -> +this.value;
            case NEG -> -this.value;
        };
    }

    @Override
    public void acceptDecl(AstDecl node) {
        var tmp = this.value;
        node.value.accept(this);
        this.env.define(node.name.image, this.value);
        this.value = tmp;
    }

    @Override
    public void acceptDef(AstDef node) {
        this.env = this.env.beginScope();
        for (var decl : node.decls) {
            decl.accept(this);
        }
        node.body.accept(this);
        this.env = this.env.endScope();
    }

    @Override
    public void acceptEmptyNode(AstEmptyNode node) {
    }

    @Override
    public void acceptVar(AstVar node) {
        this.value = this.env.lookup(node.name.image).get();
    }

}
