package icl.stages.print;

import icl.ast.AstAssign;
import icl.ast.AstBinOp;
import icl.ast.AstBool;
import icl.ast.AstBuiltin;
import icl.ast.AstCall;
import icl.ast.AstDecl;
import icl.ast.AstEmptyNode;
import icl.ast.AstField;
import icl.ast.AstFn;
import icl.ast.AstIf;
import icl.ast.AstLoop;
import icl.ast.AstNew;
import icl.ast.AstNum;
import icl.ast.AstPrint;
import icl.ast.AstRecord;
import icl.ast.AstScope;
import icl.ast.AstStr;
import icl.ast.AstTypeAlias;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;

class NodeVisitor implements AstVisitor {
    private int indent;

    public NodeVisitor() {
        this.indent = 0;
    }

    @Override
    public void acceptNum(AstNum node) {
        this.print("AcceptNum: " + node.value);
    }

    @Override
    public void acceptBool(AstBool node) {
        this.print("AcceptBool: " + node.value);
    }

    @Override
    public void acceptStr(AstStr node) {
        this.print("AcceptStr: " + node.value);
    }

    @Override
    public void acceptBinOp(AstBinOp node) {
        node.left.accept(this);
        node.right.accept(this);
        this.print("AcceptBinOp: " + PrintCommon.binOpKindToString(node.kind));
    }

    @Override
    public void acceptUnaryOp(AstUnaryOp node) {
        node.expr.accept(this);
        this.print("AcceptUnaryOp: " + PrintCommon.unaryOpKindToString(node.kind));
    }

    @Override
    public void acceptDecl(AstDecl node) {
        node.value.accept(this);
        this.print("AcceptDecl: " + node.name, " : ", node.type.toString());
    }

    @Override
    public void acceptScope(AstScope node) {

        this.print("AcceptScope");
        this.indent += 2;
        for (var stmt : node.stmts)
            stmt.accept(this);
        node.expr.accept(this);
        this.indent -= 2;
    }

    @Override
    public void acceptEmptyNode(AstEmptyNode node) {
        this.print("AcceptEmptyNode");
    }

    @Override
    public void acceptVar(AstVar node) {
        this.print("AcceptVar: " + node.name);
    }

    @Override
    public void acceptCall(AstCall call) {
        for (var arg : call.arguments)
            arg.accept(this);
        call.function.accept(this);
        this.print("AcceptCall");
    }

    @Override
    public void acceptIf(AstIf astIf) {

    }

    @Override
    public void acceptLoop(AstLoop loop) {

    }

    @Override
    public void acceptAssign(AstAssign assign) {
        assign.value.accept(this);
        this.print("Assign: " + assign.name);
    }

    @Override
    public void acceptPrint(AstPrint print) {
        print.expr.accept(this);
        this.print("AcceptPrint: newline = " + print.newline);
    }

    @Override
    public void acceptNew(AstNew anew) {
        anew.value.accept(this);
        this.print("AcceptNew");
    }

    @Override
    public void acceptFn(AstFn fn) {
        this.print("AcceptFn");
    }

    @Override
    public void acceptRecord(AstRecord record) {
        this.print("AcceptRecord");
    }

    @Override
    public void acceptField(AstField field) {
        field.value.accept(this);
        this.print("AcceptField: ", field.field);
    }

    @Override
    public void acceptTypeAlias(AstTypeAlias typeAlias) {
        this.print("AcceptTypeAlias: ", typeAlias.name, " = ", typeAlias.type.toString());
    }

    private void print(String... strings) {
        for (var i = 0; i < this.indent; ++i)
            System.out.print(" ");
        for (var string : strings)
            System.out.print(string);
        System.out.println();
    }

    @Override
    public void acceptBuiltin(AstBuiltin builtin) {
        for (var arg : builtin.args)
            arg.accept(this);
        this.print("AcceptBuiltin: ", builtin.builtin.toString());
    }
}