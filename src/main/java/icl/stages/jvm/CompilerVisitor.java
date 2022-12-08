package icl.stages.jvm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import icl.ValueType;
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
import icl.stages.jvm.stages.FunctionStage;
import icl.stages.jvm.stages.RecordStage;
import icl.stages.jvm.stages.StackFrameStage;
import icl.stages.jvm.struct.StackFrame;
import icl.stages.typecheck.TypeCheckStage;

public class CompilerVisitor implements AstVisitor {
    private static final int SL_INDEX = 3;

    private final Context context;
    private final MethodVisitor method;
    private StackFrame current_frame;

    public CompilerVisitor(Context context, MethodVisitor method) {
        this.context = context;
        this.method = method;
        this.current_frame = null;
    }

    public CompilerVisitor(Context context, MethodVisitor method, StackFrame frame) {
        this.context = context;
        this.method = method;
        this.current_frame = frame;
    }

    /**
     * Pushes a stack frame onto the stack
     * 
     * @param frame The starting frame
     * @param depth The depth of the frame to push starting from the given frame
     */
    private void pushStackFrame(StackFrame frame, int depth) {
        if (depth == 0) {
            this.method.visitVarInsn(Opcodes.ALOAD, SL_INDEX);
            return;
        }
        if (frame.parent.isEmpty())
            throw new RuntimeException("Stack frame has no parent");
        this.pushStackFrame(frame.parent.get(), depth - 1);
        this.method.visitFieldInsn(Opcodes.GETFIELD, frame.typename, "parent", frame.parent.get().descriptor);
    }

    private void pushStackFrameVar(StackFrame frame, String name) {
        var lookup = frame.lookup(name).get();
        this.pushStackFrame(frame, lookup.depth);
        this.method.visitFieldInsn(
                Opcodes.GETFIELD,
                lookup.frame.typename,
                lookup.field.field,
                lookup.field.descriptor);
    }

    @Override
    public void acceptNum(AstNum node) {
        // Push number to stack

        this.method.visitIntInsn(Opcodes.SIPUSH, node.value);
    }

    @Override
    public void acceptBool(AstBool node) {
        // Push number to stack

        var value = node.value;
        var ivalue = value ? 1 : 0;
        this.method.visitIntInsn(Opcodes.SIPUSH, ivalue);
    }

    @Override
    public void acceptStr(AstStr node) {
        // Push string to stack

        this.method.visitLdcInsn(node.value);
    }

    @Override
    public void acceptBinOp(AstBinOp node) {
        // Push left to stack
        // Push right to stack
        // Push operator instruction

        var operand_type = node.left.getAnnotation(TypeCheckStage.TYPE_KEY);
        node.left.accept(this);
        node.right.accept(this);

        switch (operand_type.getKind()) {
            case Boolean -> {
                switch (node.kind) {
                    case CMP -> {
                        var out = new Label();
                        var one = new Label();
                        this.method.visitJumpInsn(Opcodes.IF_ICMPEQ, one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 0);
                        this.method.visitJumpInsn(Opcodes.GOTO, out);
                        this.method.visitLabel(one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 1);
                        this.method.visitLabel(out);
                    }
                    case LAND -> {
                        this.method.visitInsn(Opcodes.IAND);
                    }
                    case LOR -> {
                        this.method.visitInsn(Opcodes.IOR);
                    }
                    default -> throw new IllegalStateException();
                }
            }
            case Number -> {
                switch (node.kind) {
                    case ADD -> {
                        this.method.visitInsn(Opcodes.IADD);
                    }
                    case SUB -> {
                        this.method.visitInsn(Opcodes.ISUB);
                    }
                    case MUL -> {
                        this.method.visitInsn(Opcodes.IMUL);
                    }
                    case DIV -> {
                        this.method.visitInsn(Opcodes.IDIV);
                    }
                    case CMP -> {
                        var out = new Label();
                        var one = new Label();
                        this.method.visitJumpInsn(Opcodes.IF_ICMPEQ, one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 0);
                        this.method.visitJumpInsn(Opcodes.GOTO, out);
                        this.method.visitLabel(one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 1);
                        this.method.visitLabel(out);
                    }
                    case GT -> {
                        var out = new Label();
                        var one = new Label();
                        this.method.visitJumpInsn(Opcodes.IF_ICMPGT, one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 0);
                        this.method.visitJumpInsn(Opcodes.GOTO, out);
                        this.method.visitLabel(one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 1);
                        this.method.visitLabel(out);
                    }
                    case GTE -> {
                        var out = new Label();
                        var one = new Label();
                        this.method.visitJumpInsn(Opcodes.IF_ICMPGE, one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 0);
                        this.method.visitJumpInsn(Opcodes.GOTO, out);
                        this.method.visitLabel(one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 1);
                        this.method.visitLabel(out);
                    }
                    case LT -> {
                        var out = new Label();
                        var one = new Label();
                        this.method.visitJumpInsn(Opcodes.IF_ICMPLT, one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 0);
                        this.method.visitJumpInsn(Opcodes.GOTO, out);
                        this.method.visitLabel(one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 1);
                        this.method.visitLabel(out);
                    }
                    case LTE -> {
                        var out = new Label();
                        var one = new Label();
                        this.method.visitJumpInsn(Opcodes.IF_ICMPLE, one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 0);
                        this.method.visitJumpInsn(Opcodes.GOTO, out);
                        this.method.visitLabel(one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 1);
                        this.method.visitLabel(out);
                    }
                    default -> throw new IllegalStateException();
                }
            }
            case String -> {
                switch (node.kind) {
                    case ADD -> {
                        this.method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "concat",
                                "(Ljava/lang/String;)Ljava/lang/String;", false);
                    }
                    case CMP -> {
                        this.method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "compareTo",
                                "(Ljava/lang/String;)I", false);
                        var out = new Label();
                        var one = new Label();
                        this.method.visitJumpInsn(Opcodes.IFEQ, one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 0);
                        this.method.visitJumpInsn(Opcodes.GOTO, out);
                        this.method.visitLabel(one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 1);
                        this.method.visitLabel(out);
                    }
                    default -> throw new IllegalStateException();
                }
            }
            default -> throw new IllegalStateException();
        }

    }

    @Override
    public void acceptUnaryOp(AstUnaryOp node) {
        // Push expr to stack
        // Push operator instruction

        var operand_type = node.expr.getAnnotation(TypeCheckStage.TYPE_KEY);
        node.expr.accept(this);

        switch (operand_type.getKind()) {
            case Boolean -> {
                switch (node.kind) {
                    case LNOT -> {
                        this.method.visitInsn(Opcodes.ICONST_1);
                        this.method.visitInsn(Opcodes.IXOR);
                    }
                    default -> throw new IllegalStateException();
                }
            }
            case Number -> {
                switch (node.kind) {
                    case NEG -> {
                        this.method.visitInsn(Opcodes.INEG);
                    }
                    case POS -> {
                    }
                    default -> throw new IllegalStateException();
                }
            }
            case Reference -> {
                switch (node.kind) {
                    case DEREF -> {
                        var reference_typename = this.context.getValueTypeTypename(operand_type);
                        var target_descriptor = this.context.getValueTypeDescriptor(operand_type.getReference().target);
                        this.method.visitFieldInsn(Opcodes.GETFIELD, reference_typename, "value", target_descriptor);
                    }
                    default -> throw new IllegalStateException();
                }
            }
            default -> throw new IllegalStateException();
        }
    }

    @Override
    public void acceptDecl(AstDecl node) {
        // Obtain StackFrame from annotation
        // Obtain StackFrameField using node.name
        // Push current stack frame to stack. Declarations are always made to the
        // current stack frame.
        // Push node.value to stack
        // Push PUTFIELD instruction

        var stackframe = this.current_frame;
        var lookup = stackframe.lookup(node.name).get();
        assert lookup.depth == 0;

        this.pushStackFrame(stackframe, lookup.depth);
        node.value.accept(this);
        this.method.visitFieldInsn(
                Opcodes.PUTFIELD,
                stackframe.typename,
                lookup.field.field,
                lookup.field.descriptor);
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
        // Push ALOAD to push the current stackframe to the stack
        // Push GETFIELD to get the parent stack frame
        // Push ASTORE to store the old stack frame

        var stackframe = node.getAnnotation(StackFrameStage.STACK_FRAME_KEY);
        this.current_frame = stackframe;
        Compiler.compileBasicNew(this.method, stackframe.typename);
        this.method.visitInsn(Opcodes.DUP);
        if (stackframe.parent.isEmpty())
            this.method.visitInsn(Opcodes.ACONST_NULL);
        else
            this.pushStackFrame(stackframe.parent.get(), 0);
        var parentDescriptor = switch (stackframe.parent.isPresent() ? 1 : 0) {
            case 1 -> stackframe.parent.get().descriptor;
            default -> "Ljava/lang/Object;";
        };
        this.method.visitFieldInsn(
                Opcodes.PUTFIELD,
                stackframe.typename,
                "parent",
                parentDescriptor);
        this.method.visitVarInsn(Opcodes.ASTORE, SL_INDEX);

        for (var stmt : node.stmts)
            stmt.accept(this);
        node.expr.accept(this);

        this.method.visitVarInsn(Opcodes.ALOAD, SL_INDEX);
        this.method.visitFieldInsn(
                Opcodes.GETFIELD,
                stackframe.typename,
                "parent",
                parentDescriptor);
        this.method.visitVarInsn(Opcodes.ASTORE, SL_INDEX);
        this.current_frame = stackframe.parent.orElse(null);
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

        var stackframe = this.current_frame;
        this.pushStackFrameVar(stackframe, node.name);
    }

    @Override
    public void acceptCall(AstCall call) {
        // Push arguments to stack
        // Push function to stack
        // Push INVOKEVIRTUAL instruction

        var fn_type = call.function.getAnnotation(TypeCheckStage.TYPE_KEY).getFunction();
        var call_descriptor = this.context.getFnCallDescriptor(fn_type);
        for (var arg : call.arguments)
            arg.accept(this);

        call.function.accept(this);
        this.method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/Object",
                "invoke",
                call_descriptor,
                false);
    }

    @Override
    public void acceptIf(AstIf astIf) {
        // Reserve space for fallthrough label and end label
        var labels = new Label[astIf.conditionals.size() + 2];
        var fallthrough_label_idx = labels.length - 2;
        var end_label_idx = labels.length - 1;

        for (var i = 0; i < astIf.conditionals.size(); ++i)
            labels[i] = new Label();
        labels[fallthrough_label_idx] = new Label();
        labels[end_label_idx] = new Label();

        for (var i = 0; i < astIf.conditionals.size(); ++i) {
            var conditional = astIf.conditionals.get(i);
            this.method.visitLabel(labels[i]);
            conditional.condition.accept(this);
            this.method.visitInsn(Opcodes.ICONST_1);
            this.method.visitJumpInsn(Opcodes.IF_ICMPNE, labels[i + 1]);
            conditional.expression.accept(this);
            this.method.visitJumpInsn(Opcodes.GOTO, labels[end_label_idx]);
        }

        this.method.visitLabel(labels[fallthrough_label_idx]);
        astIf.fallthrough.accept(this);
        this.method.visitLabel(labels[end_label_idx]);
    }

    @Override
    public void acceptLoop(AstLoop loop) {
        var cond_label = new Label();
        var end_label = new Label();
        this.method.visitLabel(cond_label);
        loop.condition.accept(this);
        this.method.visitInsn(Opcodes.ICONST_1);
        this.method.visitJumpInsn(Opcodes.IF_ICMPNE, end_label);
        loop.body.accept(this);
        this.method.visitJumpInsn(Opcodes.GOTO, cond_label);
        this.method.visitLabel(end_label);
    }

    @Override
    public void acceptAssign(AstAssign assign) {
        // Obtain the StackFrame from annotation
        // Obtain the StackFrameField and depth using assign.name
        // Load the stack frame at depth
        // Push assign.value to stack
        // Push PUTFIELD instruction

        var stackframe = this.current_frame;
        var lookup = stackframe.lookup(assign.name).get();
        var ltype = lookup.field.type;
        var rtype = assign.value.getAnnotation(TypeCheckStage.TYPE_KEY);

        // Special case rvalue void
        if (rtype.isKind(ValueType.Kind.Void)) {
            this.method.visitInsn(Opcodes.ACONST_NULL);
            this.method.visitFieldInsn(
                    Opcodes.PUTFIELD,
                    lookup.frame.typename,
                    lookup.field.field,
                    lookup.field.descriptor);
            return;
        }

        // Special case lvalue reference
        if (ltype.isKind(ValueType.Kind.Reference)) {
            var reference_typename = this.context.getValueTypeTypename(ltype);
            var target_descriptor = this.context.getValueTypeDescriptor(rtype);

            this.pushStackFrameVar(stackframe, assign.name);
            assign.value.accept(this);
            this.method.visitFieldInsn(
                    Opcodes.PUTFIELD,
                    reference_typename,
                    "value",
                    target_descriptor);
            return;
        }

        this.pushStackFrame(stackframe, lookup.depth);
        assign.value.accept(this);
        this.method.visitFieldInsn(
                Opcodes.PUTFIELD,
                lookup.frame.typename,
                lookup.field.field,
                lookup.field.descriptor);
    }

    @Override
    public void acceptPrint(AstPrint print) {
        var expr_type = print.expr.getAnnotation(TypeCheckStage.TYPE_KEY);
        this.method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;");
        print.expr.accept(this);

        if (expr_type.isKind(ValueType.Kind.Number)) {
            this.method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String",
                    "valueOf", "(I)Ljava/lang/String;",
                    false);
        } else if (expr_type.isKind(ValueType.Kind.Boolean)) {
            this.method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String",
                    "valueOf", "(Z)Ljava/lang/String;",
                    false);
        } else if (expr_type.isKind(ValueType.Kind.Void)) {
            this.method.visitLdcInsn("");
        } else if (!expr_type.isKind(ValueType.Kind.String)) {
            throw new RuntimeException("Cannot print type " + expr_type);
        }

        var method_name = print.newline ? "println" : "print";
        this.method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                method_name, "(Ljava/lang/String;)V", false);
    }

    @Override
    public void acceptNew(AstNew anew) {
        // Push NEW instruction to create a reference to anew.value
        // Push DUP instruction to duplicate the reference
        // Push INVOKESPECIAL instruction to call anew.value's constructor
        // Push DUP instruction to duplicate the reference
        // Push anew.value to stack
        // Push PUTFIELD instruction to set the reference value

        var value_type = anew.value.getAnnotation(TypeCheckStage.TYPE_KEY);
        var value_descriptor = this.context.getValueTypeDescriptor(value_type);
        var reference_typename = this.context.getValueTypeTypename(ValueType.createReference(value_type));

        Compiler.compileBasicNew(this.method, reference_typename);
        this.method.visitInsn(Opcodes.DUP);
        anew.value.accept(this);
        this.method.visitFieldInsn(Opcodes.PUTFIELD, reference_typename, "value", value_descriptor);
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

        var stackframe = this.current_frame;
        var function = fn.getAnnotation(FunctionStage.FUNCTION_KEY);
        // assert function.environment == stackframe;

        this.method.visitTypeInsn(Opcodes.NEW, function.typename);
        this.method.visitInsn(Opcodes.DUP);
        this.method.visitMethodInsn(Opcodes.INVOKESPECIAL, function.typename,
                "<init>", "()V", false);
        this.method.visitInsn(Opcodes.DUP);
        this.method.visitVarInsn(Opcodes.ALOAD, 0);
        this.method.visitFieldInsn(Opcodes.PUTFIELD, function.typename,
                "frame", stackframe.descriptor);

    }

    @Override
    public void acceptRecord(AstRecord record) {
        // Obtain the Record from record's annotation
        // Push NEW instruction to create the record's class
        // Push DUP instruction to duplicate the record's class
        // Push INVOKESPECIAL instruction to call the constructor
        // For each field in record.fields
        // Push DUP instruction to duplicate the record's class
        // Push field to stack
        // Push PUTFIELD instruction to set the record's field

        var rec = record.getAnnotation(RecordStage.RECORD_KEY);
        Compiler.compileBasicNew(this.method, rec.typename);

        for (var field : rec.type.fields()) {
            this.method.visitInsn(Opcodes.DUP);
            record.fields.get(field.getKey()).accept(this);
            this.method.visitFieldInsn(Opcodes.PUTFIELD, rec.typename,
                    field.getKey(), this.context.getValueTypeDescriptor(field.getValue()));
        }
    }

    @Override
    public void acceptField(AstField field) {
        // Push field.value to stack
        // Push GETFIELD instruction using field.field

        var record_vtype = field.value.getAnnotation(TypeCheckStage.TYPE_KEY);
        var record_typename = this.context.getValueTypeTypename(record_vtype);
        var field_descriptor = this.context.getValueTypeDescriptor(record_vtype.getRecord().get(field.field));

        field.value.accept(this);
        this.method.visitFieldInsn(Opcodes.GETFIELD, record_typename, field.field, field_descriptor);
    }

    @Override
    public void acceptTypeAlias(AstTypeAlias typeAlias) {
        // Do nothing
    }

}
