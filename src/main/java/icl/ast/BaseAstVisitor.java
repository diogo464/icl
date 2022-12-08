package icl.ast;

// Base visitor that visits all nodes.
public class BaseAstVisitor implements AstVisitor {

    @Override
    public void acceptNum(AstNum node) {
    }

    @Override
    public void acceptBool(AstBool node) {
    }

    @Override
    public void acceptStr(AstStr node) {
    }

    @Override
    public void acceptBinOp(AstBinOp node) {
        node.left.accept(this);
        node.right.accept(this);
    }

    @Override
    public void acceptUnaryOp(AstUnaryOp node) {
        node.expr.accept(this);
    }

    @Override
    public void acceptDecl(AstDecl node) {
        node.value.accept(this);
    }

    @Override
    public void acceptScope(AstScope node) {
        for (var stmt : node.stmts)
            stmt.accept(this);
        node.expr.accept(this);
    }

    @Override
    public void acceptEmptyNode(AstEmptyNode node) {
    }

    @Override
    public void acceptVar(AstVar node) {
    }

    @Override
    public void acceptCall(AstCall call) {
        call.function.accept(this);
        for (var arg : call.arguments)
            arg.accept(this);
    }

    @Override
    public void acceptIf(AstIf astIf) {
        for (var cond : astIf.conditionals) {
            cond.condition.accept(this);
            cond.expression.accept(this);
        }
        astIf.fallthrough.accept(this);
    }

    @Override
    public void acceptLoop(AstLoop loop) {
        loop.condition.accept(this);
        loop.body.accept(this);
    }

    @Override
    public void acceptAssign(AstAssign assign) {
        assign.value.accept(this);
    }

    @Override
    public void acceptPrint(AstPrint print) {
        print.expr.accept(this);
    }

    @Override
    public void acceptNew(AstNew anew) {
        anew.value.accept(this);
    }

    @Override
    public void acceptFn(AstFn fn) {
        fn.body.accept(this);
    }

    @Override
    public void acceptRecord(AstRecord record) {
        for (var field : record.fields.values())
            field.accept(this);
    }

    @Override
    public void acceptField(AstField field) {
        field.value.accept(this);
    }

    @Override
    public void acceptTypeAlias(AstTypeAlias typeAlias) {
    }

    @Override
    public void acceptBuiltin(AstBuiltin builtin) {
        for (var arg : builtin.args)
            arg.accept(this);
    }

}
