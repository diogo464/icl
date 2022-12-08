package icl.stages.jvm2;

import icl.ValueType;

public class Names {
    public static String typename(ValueType vtype) {
        return switch (vtype.getKind()) {
            case Alias -> throw new IllegalArgumentException("Alias has no typename");
            case Boolean -> throw new IllegalArgumentException("Boolean has no typename");
            case Function -> typename(vtype.getFunction());
            case Number -> throw new IllegalArgumentException("Number has no typename");
            case Record -> typename(vtype.getRecord());
            case Reference -> typename(vtype.getReference());
            case String -> throw new IllegalArgumentException("String has no typename");
            case Void -> "java/lang/Object";
        };
    }

    public static String typename(ValueType.Function fn) {
        var sb = new StringBuilder();
        sb.append("fn_");
        sb.append(mangle(fn.ret));
        sb.append("R");
        for (var arg : fn.args) {
            sb.append("_");
            sb.append(mangle(arg));
        }
        return sb.toString();
    }

    public static String typename(ValueType.Record record) {
        var sb = new StringBuilder();
        sb.append("record");
        for (var ftype : record.fields()) {
            sb.append("_");
            sb.append(mangle(ftype.getValue()));
        }
        return sb.toString();
    }

    public static String typename(ValueType.Reference ref) {
        var sb = new StringBuilder();
        sb.append("ref_");
        sb.append(mangle(ref.target));
        return sb.toString();
    }

    public static String descriptor(ValueType vtype) {
        return switch (vtype.getKind()) {
            case Boolean -> "I";
            case Number -> "I";
            case String -> "Ljava/lang/String;";
            default -> typenameToDescriptor(typename(vtype));
        };
    }

    public static String descriptor(ValueType.Function fn) {
        return typenameToDescriptor(typename(fn));
    }

    public static String descriptor(ValueType.Record record) {
        return typenameToDescriptor(typename(record));
    }

    public static String descriptor(ValueType.Reference ref) {
        return typenameToDescriptor(typename(ref));
    }

    public static String callDescriptor(ValueType.Function fn) {
        var sb = new StringBuilder();
        sb.append("(");
        for (var arg : fn.args) {
            sb.append(descriptor(arg));
        }
        sb.append(")");
        sb.append(descriptor(fn.ret));
        return sb.toString();
    }

    public static String typenameToDescriptor(String typename) {
        return "L" + typename + ";";
    }

    private static String mangle(ValueType vtype) {
        return switch (vtype.getKind()) {
            case Alias -> throw new IllegalStateException();
            case Boolean -> "b";
            case Function -> {
                var fn = vtype.getFunction();
                var sb = new StringBuilder();
                sb.append("f");
                sb.append(mangle(fn.ret));
                for (var arg : fn.args) {
                    sb.append(mangle(arg));
                }
                yield sb.toString();
            }
            case Number -> "n";
            case Record -> {
                var record = vtype.getRecord();
                var sb = new StringBuilder();
                sb.append("r");
                for (var ftype : record.fields()) {
                    sb.append(mangle(ftype.getValue()));
                }
                yield sb.toString();
            }
            case Reference -> {
                var ref = vtype.getReference();
                var sb = new StringBuilder();
                sb.append("r");
                sb.append(mangle(ref.target));
                yield sb.toString();
            }
            case String -> "s";
            case Void -> "v";
        };
    }
}
