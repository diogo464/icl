package icl.stages.print;

import icl.ast.AstBinOp;
import icl.ast.AstUnaryOp;

class PrintCommon {
    public static String binOpKindToString(AstBinOp.Kind kind) {
        return switch (kind) {
            case ADD -> "+";
            case DIV -> "/";
            case IDIV -> "//";
            case MUL -> "*";
            case SUB -> "-";
            case LAND -> "&&";
            case CMP -> "==";
            case GT -> ">";
            case GTE -> ">=";
            case LT -> "<";
            case LTE -> "<=";
            case LOR -> "||";
        };
    }

    public static String unaryOpKindToString(AstUnaryOp.Kind kind) {
        return switch (kind) {
            case POS -> "+";
            case NEG -> "-";
            case DEREF -> "!";
            case LNOT -> "~";
        };
    }
}
