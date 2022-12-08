package icl.stages.jvm2;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import icl.ValueType;
import icl.ast.AstFn;
import icl.ast.AstNode;
import icl.stages.typecheck.TypeCheckStage;

public class Compiler {
    public static final int SL_INDEX = 3;

    /**
     * Compiles a Main class with a main method.
     * 
     * @param env  The environment
     * @param node The node to compile
     * @return The compiled class
     */
    public static CompiledClass main(Environment env, AstNode node) {
        var main_class = new ClassWriter(0);
        main_class.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Main", null,
                "java/lang/Object", new String[] {});
        var method = main_class.visitMethod(Opcodes.ACC_PUBLIC +
                Opcodes.ACC_STATIC, "main",
                "([Ljava/lang/String;)V", null,
                null);
        method.visitCode();
        method.visitMaxs(256, 256);

        Compiler.compileBasicNew(method, env.getTypename());
        method.visitVarInsn(Opcodes.ASTORE, SL_INDEX);

        var visitor = new CompilerVisitor(env, method);
        node.accept(visitor);

        method.visitInsn(Opcodes.RETURN);
        method.visitEnd();
        main_class.visitEnd();

        return new CompiledClass("Main", main_class.toByteArray());
    }

    public static CompiledClass compile(Environment env) {
        var typename = env.getTypename();

        var cwriter = new ClassWriter(0);
        cwriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, typename, null, "java/lang/Object", new String[] {});

        var parent_descriptor = switch (env.hasParent() ? 1 : 0) {
            case 1 -> env.getParent().getDescriptor();
            default -> Names.typenameToDescriptor("java/lang/Object");
        };
        cwriter.visitField(Opcodes.ACC_PUBLIC, "parent", parent_descriptor, null, null);

        for (var field : env.fields())
            cwriter.visitField(Opcodes.ACC_PUBLIC, field.field, field.descriptor, null, null);

        Compiler.generateDefaultInitMethod(cwriter);
        cwriter.visitEnd();

        return new CompiledClass(typename, cwriter.toByteArray());
    }

    public static CompiledClass compile(ValueType.Function fn) {
        var typename = Names.typename(fn);
        var call_descriptor = Names.callDescriptor(fn);

        var cwriter = new ClassWriter(0);
        cwriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_INTERFACE, typename, null,
                "java/lang/Object", new String[] {});

        var method = cwriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT, "call", call_descriptor, null,
                null);
        method.visitEnd();
        cwriter.visitEnd();

        return new CompiledClass(typename, cwriter.toByteArray());
    }

    public static CompiledClass compile(Environment env, String typename, AstFn fn) {
        var vtype = fn.getAnnotation(TypeCheckStage.TYPE_KEY).getFunction();
        var interface_typename = Names.typename(vtype);
        var call_descriptor = Names.callDescriptor(vtype);
        var function_typename = typename;
        var environment_descriptor = env.getDescriptor();

        var cwriter = new ClassWriter(0);
        cwriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, function_typename, null, "java/lang/Object", new String[] {
                interface_typename
        });
        cwriter.visitField(Opcodes.ACC_PUBLIC, "frame", environment_descriptor, null, null);

        var method = cwriter.visitMethod(Opcodes.ACC_PUBLIC, "call", call_descriptor, null, null);
        method.visitCode();
        method.visitMaxs(256, 256);

        // Load frame field into SL INDEX
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD, function_typename, "frame", env.getDescriptor());
        method.visitVarInsn(Opcodes.ASTORE, SL_INDEX);
        var visitor = new CompilerVisitor(env, method);
        fn.body.accept(visitor);
        method.visitInsn(Opcodes.RETURN);
        method.visitEnd();

        return new CompiledClass(function_typename, cwriter.toByteArray());
    }

    public static CompiledClass compile(ValueType.Record record) {
        var typename = Names.typename(record);
        var cwriter = new ClassWriter(0);
        cwriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, typename, null, "java/lang/Object", new String[] {});

        for (var field : record.fields()) {
            var name = field.getKey();
            var type = field.getValue();
            var descriptor = Names.descriptor(type);
            cwriter.visitField(Opcodes.ACC_PUBLIC, name, descriptor, null, null);
        }

        generateDefaultInitMethod(cwriter);
        cwriter.visitEnd();
        return new CompiledClass(typename, cwriter.toByteArray());
    }

    public static CompiledClass compile(ValueType.Reference ref) {
        var typename = Names.typename(ref);
        var cwriter = new ClassWriter(0);
        cwriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, typename, null, "java/lang/Object", new String[] {});
        cwriter.visitField(Opcodes.ACC_PUBLIC, "value", Names.descriptor(ref.target), null, null);

        generateDefaultInitMethod(cwriter);
        cwriter.visitEnd();
        var bytecode = cwriter.toByteArray();
        return new CompiledClass(typename, bytecode);
    }

    /**
     * Creates a new object with the given type and calls the init method.
     * The object is left on the stack.
     * 
     * @param method   The method to compile into
     * @param typename The type of the object to create
     */
    static void compileBasicNew(MethodVisitor method, String typename) {
        method.visitTypeInsn(Opcodes.NEW, typename);
        method.visitInsn(Opcodes.DUP);
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, typename,
                "<init>", "()V", false);
    }

    private static void generateDefaultInitMethod(ClassWriter frame) {
        var init = frame.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.visitCode();
        init.visitMaxs(1, 1);
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        init.visitInsn(Opcodes.RETURN);
        init.visitEnd();
    }
}
