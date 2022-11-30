package icl.stages.typecheck.exception;

import icl.ast.AstNode;
import icl.stages.parser.ParserStage;
import icl.stages.parser.Span;

public class TypeCheckException extends RuntimeException {
    public TypeCheckException(String message) {
        super(message);
    }

    public TypeCheckException(String message, Span span) {
        super(message + " at " + span);
    }

    public TypeCheckException(String message, AstNode node) {
        this(message, node.getAnnotation(ParserStage.SPAN_KEY));
    }

}
