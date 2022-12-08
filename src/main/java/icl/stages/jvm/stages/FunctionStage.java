package icl.stages.jvm.stages;

import java.util.HashSet;
import java.util.Set;

import icl.ast.AnnotationKey;
import icl.ast.AstCall;
import icl.ast.AstFn;
import icl.ast.AstNode;
import icl.ast.BaseAstVisitor;
import icl.pipeline.PipelineStage;
import icl.stages.jvm.Compiler;
import icl.stages.jvm.Context;
import icl.stages.jvm.JvmUtils;
import icl.stages.jvm.struct.Function;
import icl.stages.jvm.struct.FunctionInterface;
import icl.stages.typecheck.TypeCheckStage;

/*-
 * This stage does 2 thing:
 * 1. Compiles each function.
 * 2. Compiles each unique function interface.
 */
public class FunctionStage implements PipelineStage<AstNode, AstNode> {

    public static AnnotationKey<Function> FUNCTION_KEY = new AnnotationKey<>("function");

    private final Context context;

    private static class Visitor extends BaseAstVisitor {
        private final Context context;
        private final Set<FunctionInterface> interfaces;

        public Visitor(Context context) {
            this.context = context;
            this.interfaces = new HashSet<>();
        }

        @Override
        public void acceptCall(AstCall node) {

        }

        @Override
        public void acceptFn(AstFn node) {
            var type = node.getAnnotation(TypeCheckStage.TYPE_KEY);
            var fntype = type.getFunction();

            var call_descriptor = this.context.getFnCallDescriptor(fntype);
            var iface_typename = this.context.generateFunctionInterfaceName();
            var iface_descriptor = JvmUtils.descriptorFromTypename(iface_typename);
            var iface = new FunctionInterface(fntype, iface_typename, iface_descriptor, call_descriptor);
            if (this.interfaces.add(iface)) {
                var compiled = Compiler.compile(this.context, iface);
                this.context.addCompiledClass(compiled);
            }

            var stackframe = node.getAnnotation(StackFrameStage.STACK_FRAME_KEY);
            var fntypename = this.context.getValueTypeTypename(type);
            var fndescriptor = this.context.getValueTypeDescriptor(type);
            var fn = new Function(node, iface, stackframe, fntypename);
            node.annotate(FUNCTION_KEY, fn);
            var compiled = Compiler.compile(this.context, fn);
            this.context.addCompiledClass(compiled);
        }
    }

    public FunctionStage(Context context) {
        this.context = context;
    }

    @Override
    public AstNode process(AstNode input) {
        var visitor = new Visitor(this.context);
        input.accept(visitor);
        return input;
    }

}
