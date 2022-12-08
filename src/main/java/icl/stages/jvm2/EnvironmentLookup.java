package icl.stages.jvm2;

public class EnvironmentLookup {
    public final Environment env;
    public final EnvironmentField field;
    public final int depth;

    public EnvironmentLookup(Environment env, EnvironmentField field, int depth) {
        this.env = env;
        this.field = field;
        this.depth = depth;
    }

}
