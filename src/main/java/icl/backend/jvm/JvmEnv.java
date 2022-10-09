package icl.backend.jvm;

class JvmEnv {
    private final NameGenerator name_generator;

    public JvmEnv(NameGenerator name_generator) {
        this.name_generator = name_generator;
    }

    public StackFrame pushFrame(int varcount) {return null;}
}
