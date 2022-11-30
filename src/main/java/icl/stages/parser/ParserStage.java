package icl.stages.parser;

import java.io.InputStream;

import icl.ast.AstNode;
import icl.pipeline.PipelineStage;
import icl.stages.parser.exception.ParserException;

public class ParserStage implements PipelineStage<InputStream, AstNode> {

    @Override
    public AstNode process(InputStream input) {
        try {
            return Parser.parse(input);
        } catch (ParseException e) {
            throw new ParserException(e);
        }
    }

}
