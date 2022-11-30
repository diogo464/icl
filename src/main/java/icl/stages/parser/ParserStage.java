package icl.stages.parser;

import java.io.InputStream;

import icl.ast.AnnotationKey;
import icl.ast.AstNode;
import icl.pipeline.PipelineStage;
import icl.stages.parser.exception.ParserException;

public class ParserStage implements PipelineStage<InputStream, AstNode> {

    public static final AnnotationKey<Span> SPAN_KEY = Parser.SPAN_KEY;

    @Override
    public AstNode process(InputStream input) {
        try {
            return Parser.parse(input);
        } catch (ParseException e) {
            throw new ParserException(e);
        }
    }

}
