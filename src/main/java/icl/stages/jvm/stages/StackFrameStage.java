package icl.stages.jvm.stages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import icl.ast.AnnotationKey;
import icl.ast.AstDecl;
import icl.ast.AstFn;
import icl.ast.AstNode;
import icl.ast.AstScope;
import icl.ast.BaseAstVisitor;
import icl.pipeline.PipelineStage;
import icl.stages.jvm.Compiler;
import icl.stages.jvm.Context;
import icl.stages.jvm.struct.StackFrame;
import icl.stages.jvm.struct.StackFrameField;
import icl.stages.typecheck.TypeCheckStage;

/*-
 * This stage does 2 thing:
 * 1. Annotates each AstScope node with the stack frame that it represents.
 * 2. Compiles each stack frame.
 * 
 * Annotations:
 *  AstScope:
 *      - STACK_FRAME_KEY: The stack frame that the node belongs to.
 */
public class StackFrameStage implements PipelineStage<AstNode, AstNode> {
    public static final AnnotationKey<StackFrame> STACK_FRAME_KEY = new AnnotationKey<>("jvm-stack-frame");

    // Visits AstNodes and gathers all variable declarations.
    // Does not visit into scopes.
    private static class FieldVisitor extends BaseAstVisitor {

        private final Context context;
        private final List<StackFrameField> fields;
        private int varcounter;

        public FieldVisitor(Context context) {
            this.context = context;
            this.fields = new ArrayList<>();
            this.varcounter = 0;
        }

        public List<StackFrameField> getFields() {
            return Collections.unmodifiableList(List.copyOf(this.fields));
        }

        @Override
        public void acceptDecl(AstDecl node) {
            var name = node.name;
            var type = node.value.getAnnotation(TypeCheckStage.TYPE_KEY);
            var field = "var_" + this.varcounter++;
            var descriptor = this.context.getValueTypeDescriptor(type);
            this.fields.add(new StackFrameField(name, type, field, descriptor));
        }

        @Override
        public void acceptScope(AstScope scope) {
            // Dont visit into scopes
        }

        @Override
        public void acceptFn(AstFn fn) {
            // Dont visit into functions
        }
    }

    private static class Visitor extends BaseAstVisitor {

        private final Context context;
        private final Stack<StackFrame> frames = new Stack<>();

        public Visitor(Context context) {
            this.context = context;
        }

        @Override
        public void acceptScope(AstScope node) {
            var parent = this.frames.isEmpty() ? null : this.frames.peek();
            var stackframe = this.stackFrameFromScope(Optional.ofNullable(parent), node);
            var compiled = Compiler.compile(stackframe);
            this.context.addCompiledClass(compiled);
            this.frames.push(stackframe);
            node.annotate(STACK_FRAME_KEY, stackframe);
            for (var stmt : node.stmts)
                stmt.accept(this);
            node.expr.accept(this);
            this.frames.pop();
        }

        @Override
        public void acceptFn(AstFn fn) {
            var stackframe = this.frames.peek();
            fn.annotate(STACK_FRAME_KEY, stackframe);
        }

        private StackFrame stackFrameFromScope(Optional<StackFrame> parent, AstScope scope) {
            var typename = this.context.generateStackFrameName();
            var fieldVisitor = new FieldVisitor(context);

            for (var stmt : scope.stmts)
                stmt.accept(fieldVisitor);
            scope.expr.accept(fieldVisitor);

            return new StackFrame(typename, parent, fieldVisitor.getFields());
        }

    }

    private final Context context;

    public StackFrameStage(Context context) {
        this.context = context;
    }

    @Override
    public AstNode process(AstNode input) {
        input.accept(new Visitor(this.context));
        return input;
    }

}
