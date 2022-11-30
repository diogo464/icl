package icl.pipeline;

public class JointPipelineStage<In, Out> implements PipelineStage<In, Out> {

    private final PipelineStage<In, Object> first;
    private final PipelineStage<Object, Out> second;

    @SuppressWarnings("unchecked")
    public <X> JointPipelineStage(PipelineStage<In, X> first, PipelineStage<X, Out> second) {
        this.first = (PipelineStage<In, Object>) first;
        this.second = (PipelineStage<Object, Out>) second;
    }

    @Override
    public Out process(In input) {
        var intermediate = first.process(input);
        return second.process(intermediate);
    }

}
