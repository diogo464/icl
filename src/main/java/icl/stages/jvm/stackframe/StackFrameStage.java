package icl.stages.jvm.stackframe;

import java.util.Optional;
import java.util.Stack;

import icl.ast.AstAssign;
import icl.ast.AstBinOp;
import icl.ast.AstBool;
import icl.ast.AstCall;
import icl.ast.AstDecl;
import icl.ast.AstEmptyNode;
import icl.ast.AstField;
import icl.ast.AstFn;
import icl.ast.AstIf;
import icl.ast.AstLoop;
import icl.ast.AstNew;
import icl.ast.AstNode;
import icl.ast.AstNum;
import icl.ast.AstPrint;
import icl.ast.AstRecord;
import icl.ast.AstScope;
import icl.ast.AstStr;
import icl.ast.AstTypeAlias;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;
import icl.pipeline.PipelineStage;
import icl.stages.jvm.Compiler;
import icl.stages.jvm.Context;
import icl.stages.jvm.NameGenerator;

public class StackFrameStage implements PipelineStage<AstNode, AstNode> {

    private static class Visitor implements AstVisitor {

        private final NameGenerator nameGenerator;
        private final Context context;
        private final Stack<StackFrame> frames = new Stack<>();

        public Visitor(NameGenerator nameGenerator, Context context) {
            this.nameGenerator = nameGenerator;
            this.context = context;
        }

        private void annotateNode(AstNode node) {
            var frame = this.frames.peek();
            node.annotate(Compiler.STACK_FRAME_KEY, frame);
        }

        @Override
        public void acceptNum(AstNum node) {
            this.annotateNode(node);
        }

        @Override
        public void acceptBool(AstBool node) {
            this.annotateNode(node);
        }

        @Override
        public void acceptStr(AstStr node) {
            this.annotateNode(node);
        }

        @Override
        public void acceptBinOp(AstBinOp node) {
            node.left.accept(this);
            node.right.accept(this);
            this.annotateNode(node);
        }

        @Override
        public void acceptUnaryOp(AstUnaryOp node) {
            node.expr.accept(this);
            this.annotateNode(node);
        }

        @Override
        public void acceptDecl(AstDecl node) {
            node.value.accept(this);
            this.annotateNode(node);
        }

        @Override
        public void acceptScope(AstScope node) {
            var parent = this.frames.isEmpty() ? null : this.frames.peek();
            var stackframe = StackFrame.fromScope(this.nameGenerator, this.context, Optional.ofNullable(parent), node);
            this.context.registerStackFrame(stackframe);
            this.frames.push(stackframe);
            this.annotateNode(node);
            for (var stmt : node.stmts)
                stmt.accept(this);
            node.expr.accept(this);
            this.frames.pop();
        }

        @Override
        public void acceptEmptyNode(AstEmptyNode node) {
            this.annotateNode(node);
        }

        @Override
        public void acceptVar(AstVar node) {
            this.annotateNode(node);
        }

        @Override
        public void acceptCall(AstCall call) {
            for (var arg : call.arguments)
                arg.accept(this);
            call.function.accept(this);
            this.annotateNode(call);
        }

        @Override
        public void acceptIf(AstIf astIf) {
            for (var cond : astIf.conditionals) {
                cond.condition.accept(this);
                cond.expression.accept(this);
            }
            astIf.fallthrough.accept(this);
            this.annotateNode(astIf);
        }

        @Override
        public void acceptLoop(AstLoop loop) {
            loop.condition.accept(this);
            loop.body.accept(this);
            this.annotateNode(loop);
        }

        @Override
        public void acceptAssign(AstAssign assign) {
            assign.value.accept(this);
            this.annotateNode(assign);
        }

        @Override
        public void acceptPrint(AstPrint print) {
            print.expr.accept(this);
            this.annotateNode(print);
        }

        @Override
        public void acceptNew(AstNew anew) {
            anew.value.accept(this);
            this.annotateNode(anew);
        }

        @Override
        public void acceptFn(AstFn fn) {
            // TODO: Implement this
        }

        @Override
        public void acceptRecord(AstRecord record) {
            record.fields.values().forEach(field -> field.accept(this));
            this.annotateNode(record);
        }

        @Override
        public void acceptField(AstField field) {
            field.value.accept(this);
            this.annotateNode(field);
        }

        @Override
        public void acceptTypeAlias(AstTypeAlias typeAlias) {
        }

    }

    private final NameGenerator nameGenerator;
    private final Context context;

    public StackFrameStage(NameGenerator nameGenerator, Context context) {
        this.nameGenerator = nameGenerator;
        this.context = context;
    }

    @Override
    public AstNode process(AstNode input) {
        // var scope = new AstScope(List.of(input), new AstEmptyNode());
        var visitor = new Visitor(this.nameGenerator, this.context);
        input.accept(visitor);
        // scope.accept(visitor);
        return input;
    }

}
