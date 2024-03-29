package icl.pipeline;

import java.util.function.Function;

public class Pipeline<In, Out> implements PipelineStage<In, Out> {

    private final PipelineStage<In, Out> stage;

    private Pipeline(PipelineStage<In, Out> stage) {
        this.stage = stage;
    }

    @Override
    public Out process(In input) {
        return this.stage.process(input);
    }

    public <T> Pipeline<In, T> add(PipelineStage<Out, T> next) {
        return new Pipeline<>(new JointPipelineStage<>(this.stage, next));
    }

    public static <T> Pipeline<T, T> begin(PipelineStage<T, T> stage) {
        return new Pipeline<>(stage);
    }

    public static <T> PipelineStage<T, T> forward() {
        return new ForwardPipelineStage<>();
    }

    public static <I, R> PipelineStage<I, R> function(Function<I, R> function) {
        return new PipelineStage<I, R>() {
            @Override
            public R process(I input) {
                return function.apply(input);
            }
        };
    }
}
