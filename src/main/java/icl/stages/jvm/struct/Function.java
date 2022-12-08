package icl.stages.jvm.struct;

import icl.ast.AstFn;
import icl.stages.jvm.JvmUtils;

public class Function {
    // The AST node of this function.
    // This is used to generate the function's code.
    public final AstFn node;

    // Interface of this function.
    public final FunctionInterface iface;

    // The environment of this function.
    public final StackFrame environment;

    // Name of the JVM class.
    public final String typename;

    // Type descriptor of the function.
    // This is the JVM type descriptor of the function's class.
    public final String descriptor;

    public Function(AstFn node, FunctionInterface iface, StackFrame environment, String typename) {
        this.node = node;
        this.iface = iface;
        this.environment = environment;
        this.typename = typename;
        this.descriptor = JvmUtils.descriptorFromTypename(typename);
    }
}
