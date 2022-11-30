package icl.stages.parser.exception;

import icl.stages.parser.ParseException;

public class ParserException extends RuntimeException {
    public ParserException(String message) {
        super(message);
    }

    public ParserException(ParseException e) {
        super(e);
    }
}
