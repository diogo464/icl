package icl.stages.jvm;

public class JvmUtils {
    public static String descriptorFromTypename(String typename) {
        return "L" + typename + ";";
    }
}
