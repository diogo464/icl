package icl.pipeline;

class ForwardPipelineStage<T> implements PipelineStage<T, T> {

    @Override
    public T process(T input) {
        return input;
    }
}
