package icl.ast;

import icl.ValueType;

public class AstType {
    public static enum Kind {
        Builtin,
        Alias,
    }

    public final Kind kind;
    // Valid if kind == Kind.Builtin
    public final ValueType builtin;
    // Valid if kind == Kind.Alias
    public final String alias;

    public AstType(ValueType builtin) {
        this.kind = Kind.Builtin;
        this.builtin = builtin;
        this.alias = null;
    }

    public AstType(String alias) {
        this.kind = Kind.Alias;
        this.builtin = null;
        this.alias = alias;
    }
}
