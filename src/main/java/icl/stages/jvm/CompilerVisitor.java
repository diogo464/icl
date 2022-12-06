package icl.stages.jvm;

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
import icl.ast.AstNum;
import icl.ast.AstPrint;
import icl.ast.AstRecord;
import icl.ast.AstScope;
import icl.ast.AstStr;
import icl.ast.AstTypeAlias;
import icl.ast.AstUnaryOp;
import icl.ast.AstVar;
import icl.ast.AstVisitor;

public class CompilerVisitor implements AstVisitor {

    @Override
    public void acceptNum(AstNum node) {
        // Push number to stack
    }

    @Override
    public void acceptBool(AstBool node) {
        // Push number to stack
    }

    @Override
    public void acceptStr(AstStr node) {
        // Push string to stack
    }

    @Override
    public void acceptBinOp(AstBinOp node) {
        // Push left to stack
        // Push right to stack
        // Push operator instruction
    }

    @Override
    public void acceptUnaryOp(AstUnaryOp node) {
        // Push expr to stack
        // Push operator instruction
    }

    @Override
    public void acceptDecl(AstDecl node) {
        // Obtain StackFrame from annotation
        // Obtain StackFrameField using node.name
        // Push current stack frame to stack. Declarations are always made to the
        // current stack frame.
        // Push node.value to stack
        // Push PUTFIELD instruction
    }

    @Override
    public void acceptScope(AstScope node) {
        // Obtain the scope's StackFrame from annotation
        // Push NEW instruction to create new stack frame
        // Push DUP instruction to duplicate the stack frame
        // Push INVOKESPECIAL instruction to call the stack frame's constructor
        // Push DUP instruction to duplicate the stack frame
        // Push ALOAD to push the current stackframe to the stack
        // Push PUTFIELD instruction to set the new stack frame's parent
        // Push ASTORE to store the new stack frame
        // Compile statements and the expression
    }

    @Override
    public void acceptEmptyNode(AstEmptyNode node) {
        // Do nothing
    }

    @Override
    public void acceptVar(AstVar node) {
        // Obtain StackFrame from annotation
        // Obtain StackFrameField and depth using node.name
        // Load the stack frame at depth
        // Push GETFIELD instruction
    }

    @Override
    public void acceptCall(AstCall call) {
        // Push arguments to stack
        // Push function to stack
        // Push INVOKEVIRTUAL instruction
    }

    @Override
    public void acceptIf(AstIf astIf) {
        // // Reserve space for fallthrough label and end label
        // var labels = new Label[astIf.conditionals.size() + 2];
        // var fallthrough_label_idx = labels.length - 2;
        // var end_label_idx = labels.length - 1;

        // for (var i = 0; i < astIf.conditionals.size(); ++i)
        // labels[i] = new Label();
        // labels[fallthrough_label_idx] = new Label();
        // labels[end_label_idx] = new Label();

        // for (var i = 0; i < astIf.conditionals.size(); ++i) {
        // var conditional = astIf.conditionals.get(i);
        // conditional.condition.accept(this);
        // this.visitor.visitInsn(Opcodes.ICONST_1);
        // this.visitor.visitJumpInsn(Opcodes.IF_ICMPNE, labels[i + 1]);
        // conditional.expression.accept(this);
        // this.visitor.visitJumpInsn(Opcodes.GOTO, labels[end_label_idx]);
        // }

        // this.visitor.visitLabel(labels[fallthrough_label_idx]);
        // astIf.fallthrough.accept(this);
        // this.visitor.visitLabel(labels[end_label_idx]);
    }

    @Override
    public void acceptLoop(AstLoop loop) {
        // var cond_label = new Label();
        // var end_label = new Label();
        // this.visitor.visitLabel(cond_label);
        // loop.condition.accept(this);
        // this.visitor.visitInsn(Opcodes.ICONST_1);
        // this.visitor.visitJumpInsn(Opcodes.IF_ICMPNE, end_label);
        // loop.body.accept(this);
        // this.visitor.visitJumpInsn(Opcodes.GOTO, cond_label);
        // this.visitor.visitLabel(end_label);
    }

    @Override
    public void acceptAssign(AstAssign assign) {
        // Obtain the StackFrame from annotation
        // Obtain the StackFrameField and depth using assign.name
        // Load the stack frame at depth
        // Push assign.value to stack
        // Push PUTFIELD instruction
    }

    @Override
    public void acceptPrint(AstPrint print) {
        // TODO: For now this only works with ints
        // this.visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
        // "Ljava/io/PrintStream;");
        // print.expr.accept(this);
        // // TODO: Descriptor of valueOf is wrong, only works for int
        // this.visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String",
        // "valueOf", "(I)Ljava/lang/String;",
        // false);
        // var method_name = print.newline ? "println" : "print";
        // this.visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
        // method_name, "(Ljava/lang/String;)V", false);
    }

    @Override
    public void acceptNew(AstNew anew) {
        // Push NEW instruction to create a reference to anew.value
        // Push DUP instruction to duplicate the reference
        // Push INVOKESPECIAL instruction to call anew.value's constructor
        // Push DUP instruction to duplicate the reference
        // Push anew.value to stack
        // Push PUTFIELD instruction to set the reference value
    }

    @Override
    public void acceptFn(AstFn fn) {
        // Obtain the function's ValueType from annotation
        // Obtain the Function from fn's annotation
        // Push NEW instruction to create the function's class
        // Push DUP instruction to duplicate the function's class
        // Push INVOKESPECIAL instruction to call the constructor
        // Push DUP instruction to duplicate the function's class
        // Push ALOAD to push the current stackframe to the stack
        // Push PUTFIELD instruction to set the function's environment
        // Assert that the StackFrame associated in the function's annotation
        // is the same as Function.environment
    }

    @Override
    public void acceptRecord(AstRecord record) {
        // Obtain the Record from record's annotation
        // Push NEW instruction to create the record's class
        // Push DUP instruction to duplicate the record's class
        // Push INVOKESPECIAL instruction to call the constructor
        // For each field in record.fields
        //  Push DUP instruction to duplicate the record's class
        //  Push field to stack
        //  Push PUTFIELD instruction to set the record's field
    }

    @Override
    public void acceptField(AstField field) {
        // Push field.value to stack
        // Push GETFIELD instruction using field.field
    }

    @Override
    public void acceptTypeAlias(AstTypeAlias typeAlias) {
        // Do nothing
    }

}
