package icl.stages.jvm;

public class JvmUtils {
    public static String typedescriptorFromTypename(String typename) {
        return "L" + typename + ";";
    }
}
