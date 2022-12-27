package icl.stages.jvm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import icl.ValueType;
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
import icl.stages.jvm.Context.Namespace;
import icl.stages.typecheck.TypeCheckStage;

public class CompilerVisitor implements AstVisitor {

    private static final int SL_INDEX = Compiler.SL_INDEX;
    private static final int SL_SCRATCH = Compiler.SL_SCRATCH;

    private final MethodVisitor method;
    private Environment env;

    public CompilerVisitor(Environment env, MethodVisitor method) {
        this.env = env;
        this.method = method;
    }

    /**
     * Pushes a stack frame onto the stack
     * 
     * @param frame The starting frame
     * @param depth The depth of the frame to push starting from the given frame
     */
    private void pushEnv(Environment frame, int depth) {
        var current = frame;
        this.method.visitVarInsn(Opcodes.ALOAD, SL_INDEX);
        for (var i = 0; i < depth; i++) {
            var typename = current.getTypename();
            var parent_descriptor = current.getParent().getDescriptor();
            this.method.visitFieldInsn(Opcodes.GETFIELD, typename, "parent", parent_descriptor);
            current = current.getParent();
        }
    }

    private void pushVar(String name) {
        var lookup = this.env.lookup(name).get();
        this.pushEnv(this.env, lookup.depth);
        this.method.visitFieldInsn(
                Opcodes.GETFIELD,
                lookup.env.getTypename(),
                lookup.field.field,
                lookup.field.descriptor);
    }

    /**
     * Assign the value on the top of the stack to the given variable.
     * The variable on the stack must have the same type as the variable being
     * assigned to.
     * 
     * @param name The name of the variable to assign to
     */
    private void assignVar(String name) {
        var lookup = this.env.lookup(name).get();
        this.method.visitVarInsn(Opcodes.ASTORE, SL_SCRATCH);
        this.pushEnv(this.env, lookup.depth);
        this.method.visitVarInsn(Opcodes.ALOAD, SL_SCRATCH);
        this.method.visitFieldInsn(
                Opcodes.PUTFIELD,
                lookup.env.getTypename(),
                lookup.field.field,
                lookup.field.descriptor);
    }

    private void beginEnv() {
        var new_env = this.env.begin();
        var parent_descriptor = this.env.getDescriptor();

        Compiler.compileBasicNew(this.method, new_env.getTypename());
        this.method.visitInsn(Opcodes.DUP);
        this.pushEnv(this.env, 0);

        this.method.visitFieldInsn(
                Opcodes.PUTFIELD,
                new_env.getTypename(),
                "parent",
                parent_descriptor);
        this.method.visitVarInsn(Opcodes.ASTORE, SL_INDEX);
        this.env = new_env;
    }

    private void endEnv() {
        var context = this.env.getContext();
        context.compile(this.env);
        this.method.visitVarInsn(Opcodes.ALOAD, SL_INDEX);
        this.method.visitFieldInsn(
                Opcodes.GETFIELD,
                this.env.getTypename(),
                "parent",
                this.env.getParent().getDescriptor());
        this.method.visitVarInsn(Opcodes.ASTORE, SL_INDEX);
        this.env = this.env.end();
    }

    @Override
    public void acceptNum(AstNum node) {
        this.method.visitLdcInsn(node.value);
    }

    @Override
    public void acceptBool(AstBool node) {
        var value = node.value;
        var ivalue = value ? 1 : 0;
        this.method.visitIntInsn(Opcodes.SIPUSH, ivalue);
    }

    @Override
    public void acceptStr(AstStr node) {
        this.method.visitLdcInsn(node.value);
    }

    @Override
    public void acceptBinOp(AstBinOp node) {
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
                        this.method.visitInsn(Opcodes.DADD);
                    }
                    case SUB -> {
                        this.method.visitInsn(Opcodes.DSUB);
                    }
                    case MUL -> {
                        this.method.visitInsn(Opcodes.DMUL);
                    }
                    case DIV -> {
                        this.method.visitInsn(Opcodes.DDIV);
                    }
                    case IDIV -> {
                        this.method.visitInsn(Opcodes.DDIV);
                        this.method.visitInsn(Opcodes.D2I);
                        this.method.visitInsn(Opcodes.I2D);
                    }
                    case CMP -> {
                        var out = new Label();
                        var one = new Label();
                        this.method.visitInsn(Opcodes.DCMPL);
                        this.method.visitJumpInsn(Opcodes.IFEQ, one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 0);
                        this.method.visitJumpInsn(Opcodes.GOTO, out);
                        this.method.visitLabel(one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 1);
                        this.method.visitLabel(out);
                    }
                    case GT -> {
                        var out = new Label();
                        var one = new Label();
                        this.method.visitInsn(Opcodes.DCMPL);
                        this.method.visitJumpInsn(Opcodes.IFGT, one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 0);
                        this.method.visitJumpInsn(Opcodes.GOTO, out);
                        this.method.visitLabel(one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 1);
                        this.method.visitLabel(out);
                    }
                    case GTE -> {
                        var out = new Label();
                        var one = new Label();
                        this.method.visitInsn(Opcodes.DCMPL);
                        this.method.visitJumpInsn(Opcodes.IFGE, one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 0);
                        this.method.visitJumpInsn(Opcodes.GOTO, out);
                        this.method.visitLabel(one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 1);
                        this.method.visitLabel(out);
                    }
                    case LT -> {
                        var out = new Label();
                        var one = new Label();
                        this.method.visitInsn(Opcodes.DCMPL);
                        this.method.visitJumpInsn(Opcodes.IFLT, one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 0);
                        this.method.visitJumpInsn(Opcodes.GOTO, out);
                        this.method.visitLabel(one);
                        this.method.visitIntInsn(Opcodes.SIPUSH, 1);
                        this.method.visitLabel(out);
                    }
                    case LTE -> {
                        var out = new Label();
                        var one = new Label();
                        this.method.visitInsn(Opcodes.DCMPL);
                        this.method.visitJumpInsn(Opcodes.IFLE, one);
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
                        this.method.visitInsn(Opcodes.DNEG);
                    }
                    case POS -> {
                    }
                    default -> throw new IllegalStateException();
                }
            }
            case Reference -> {
                switch (node.kind) {
                    case DEREF -> {
                        var reference_typename = Names.typename(operand_type);
                        var target_descriptor = Names.descriptor(operand_type.getReference().target);
                        this.env.getContext().compile(operand_type.getReference());
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
        var vtype = node.value.getAnnotation(TypeCheckStage.TYPE_KEY);
        var field = this.env.define(node.name, vtype);

        this.pushEnv(this.env, 0);
        node.value.accept(this);
        this.method.visitFieldInsn(
                Opcodes.PUTFIELD,
                this.env.getTypename(),
                field.field,
                field.descriptor);
    }

    @Override
    public void acceptScope(AstScope node) {
        this.beginEnv();
        for (var stmt : node.stmts)
            stmt.accept(this);
        node.expr.accept(this);
        this.endEnv();
    }

    @Override
    public void acceptEmptyNode(AstEmptyNode node) {
    }

    @Override
    public void acceptVar(AstVar node) {
        this.pushVar(node.name);
    }

    @Override
    public void acceptCall(AstCall call) {
        var ftype = call.function.getAnnotation(TypeCheckStage.TYPE_KEY).getFunction();
        var interface_typename = Names.typename(ftype);
        var call_descriptor = Names.callDescriptor(ftype);

        call.function.accept(this);

        for (var arg : call.arguments)
            arg.accept(this);

        this.method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                interface_typename,
                "call",
                call_descriptor,
                true);
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
        var lookup = this.env.lookup(assign.name).get();
        var ltype = lookup.field.type;
        var rtype = assign.value.getAnnotation(TypeCheckStage.TYPE_KEY);

        // Special case rvalue void
        if (rtype.isKind(ValueType.Kind.Void)) {
            this.method.visitInsn(Opcodes.ACONST_NULL);
            this.method.visitFieldInsn(
                    Opcodes.PUTFIELD,
                    lookup.env.getTypename(),
                    lookup.field.field,
                    lookup.field.descriptor);
            return;
        }

        // Special case lvalue reference
        if (ltype.isKind(ValueType.Kind.Reference)) {
            var reference_typename = Names.typename(ltype);
            var target_descriptor = Names.descriptor(rtype);

            this.pushVar(assign.name);
            assign.value.accept(this);
            this.method.visitFieldInsn(
                    Opcodes.PUTFIELD,
                    reference_typename,
                    "value",
                    target_descriptor);
            return;
        }

        this.pushEnv(this.env, lookup.depth);
        assign.value.accept(this);
        this.method.visitFieldInsn(
                Opcodes.PUTFIELD,
                lookup.env.getTypename(),
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
            if (print.nodecimal) {
                this.method.visitInsn(Opcodes.D2I);
                this.method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String",
                        "valueOf", "(I)Ljava/lang/String;", false);
            } else {
                this.method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String",
                        "valueOf", "(D)Ljava/lang/String;", false);
            }
        } else if (expr_type.isKind(ValueType.Kind.Boolean)) {
            this.method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String",
                    "valueOf", "(Z)Ljava/lang/String;", false);
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
        var value_type = anew.value.getAnnotation(TypeCheckStage.TYPE_KEY);
        var value_descriptor = Names.descriptor(value_type);
        var reference_typename = Names.typename(ValueType.createReference(value_type));

        Compiler.compileBasicNew(this.method, reference_typename);
        this.method.visitInsn(Opcodes.DUP);
        anew.value.accept(this);
        this.method.visitFieldInsn(Opcodes.PUTFIELD, reference_typename, "value", value_descriptor);
    }

    @Override
    public void acceptFn(AstFn fn) {
        var context = this.env.getContext();
        var function_type = fn.getAnnotation(TypeCheckStage.TYPE_KEY).getFunction();
        var function_typename = context.generate(Namespace.FUNCTION);
        var compiled = Compiler.compile(this.env, function_typename, fn);
        context.emit(compiled);
        context.compile(function_type);

        Compiler.compileBasicNew(this.method, function_typename);
        this.method.visitInsn(Opcodes.DUP);
        this.pushEnv(this.env, 0);
        this.method.visitFieldInsn(Opcodes.PUTFIELD, function_typename, "frame", this.env.getDescriptor());
    }

    @Override
    public void acceptRecord(AstRecord record) {
        var context = this.env.getContext();
        var vtype = record.getAnnotation(TypeCheckStage.TYPE_KEY).getRecord();
        var record_typename = Names.typename(vtype);

        context.compile(vtype);
        Compiler.compileBasicNew(this.method, record_typename);

        for (var field : vtype.fields()) {
            this.method.visitInsn(Opcodes.DUP);
            record.fields.get(field.getKey()).accept(this);
            this.method.visitFieldInsn(Opcodes.PUTFIELD, record_typename, field.getKey(),
                    Names.descriptor(field.getValue()));
        }
    }

    @Override
    public void acceptField(AstField field) {
        var rtype = field.value.getAnnotation(TypeCheckStage.TYPE_KEY).getRecord();
        var record_typename = Names.typename(rtype);
        var field_descriptor = Names.descriptor(rtype.get(field.field));

        field.value.accept(this);
        this.method.visitFieldInsn(Opcodes.GETFIELD, record_typename, field.field, field_descriptor);
    }

    @Override
    public void acceptTypeAlias(AstTypeAlias typeAlias) {
        // Do nothing
    }

    @Override
    public void acceptBuiltin(AstBuiltin builtin) {
        for (var arg : builtin.args)
            arg.accept(this);

        switch (builtin.builtin) {
            case ABS -> {
                this.method.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Math",
                        "abs",
                        "(D)D",
                        false);
            }
            case COS -> {
                this.method.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Math",
                        "cos",
                        "(D)D",
                        false);
            }
            case MAX -> {
                this.method.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Math",
                        "max",
                        "(DD)D",
                        false);
            }
            case MIN -> {
                this.method.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Math",
                        "min",
                        "(DD)D",
                        false);
            }
            case PI -> {
                this.method.visitLdcInsn((double) Math.PI);
            }
            case POW -> {
                this.method.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Math",
                        "pow",
                        "(DD)D",
                        false);
            }
            case SIN -> {
                this.method.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Math",
                        "sin",
                        "(D)D",
                        false);
            }
            case SQRT -> {
                this.method.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Math",
                        "sqrt",
                        "(D)D",
                        false);
            }
            case TAN -> {
                this.method.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Math",
                        "tan",
                        "(D)D",
                        false);
            }
            case RAND -> {
                this.method.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Math",
                        "random",
                        "()D",
                        false);
            }
        }
    }

}
