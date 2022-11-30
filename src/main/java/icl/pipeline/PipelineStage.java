package icl.pipeline;

public interface PipelineStage<In, Out> {
    public Out process(In input);
}
