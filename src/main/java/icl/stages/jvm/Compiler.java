package icl.stages.jvm;

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
        var context = env.getContext();
        var ftype = fn.getAnnotation(TypeCheckStage.TYPE_KEY).getFunction();
        var interface_typename = Names.typename(ftype);
        var call_descriptor = Names.callDescriptor(ftype);
        var function_typename = typename;
        var environment_descriptor = env.getDescriptor();

        var cwriter = new ClassWriter(0);
        cwriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, function_typename, null, "java/lang/Object", new String[] {
                interface_typename
        });
        cwriter.visitField(Opcodes.ACC_PUBLIC, "frame", environment_descriptor, null, null);

        generateDefaultInitMethod(cwriter);

        var method = cwriter.visitMethod(Opcodes.ACC_PUBLIC, "call", call_descriptor, null, null);
        method.visitCode();
        method.visitMaxs(256, 256);

        // Prepare new environment to store arguments in
        var fenv = env.begin();
        Compiler.compileBasicNew(method, fenv.getTypename());
        method.visitInsn(Opcodes.DUP);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD, function_typename, "frame", environment_descriptor);
        method.visitFieldInsn(Opcodes.PUTFIELD, fenv.getTypename(), "parent", environment_descriptor);

        for (var i = 1; i <= fn.arguments.size(); ++i) {
            var arg = fn.arguments.get(i - 1);
            var arg_type = ftype.args.get(i - 1);
            var arg_name = arg.name;
            var arg_index = i;

            // Duplicate the environment
            method.visitInsn(Opcodes.DUP);

            // Load the argument to the stack
            switch (arg_type.getKind()) {
                case Boolean -> method.visitVarInsn(Opcodes.ILOAD, arg_index);
                case Number -> method.visitVarInsn(Opcodes.FLOAD, arg_index);
                case Function, Record, Reference, String -> method.visitVarInsn(Opcodes.ALOAD, arg_index);
                default -> throw new IllegalStateException();
            }

            // Define the argument in the environment
            var field = fenv.define(arg_name, arg_type);

            // Store the argument in the environment
            method.visitFieldInsn(Opcodes.PUTFIELD, fenv.getTypename(), field.field, field.descriptor);
        }

        // Store the environment in the frame field
        method.visitVarInsn(Opcodes.ASTORE, SL_INDEX);

        var visitor = new CompilerVisitor(fenv, method);
        fn.body.accept(visitor);
        context.compile(fenv);

        var return_type = ftype.ret;
        switch (return_type.getKind()) {
            case Boolean -> method.visitInsn(Opcodes.IRETURN);
            case Number -> method.visitInsn(Opcodes.FRETURN);
            case Function, Record, Reference, String -> method.visitInsn(Opcodes.ARETURN);
            case Void -> method.visitInsn(Opcodes.RETURN);
            default -> throw new IllegalStateException();
        }
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
