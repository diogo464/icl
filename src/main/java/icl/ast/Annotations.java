package icl.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Annotations {
    private final Map<AnnotationKey<?>, Object> annotations;

    public Annotations() {
        this.annotations = new HashMap<>();
    }

    public <T> void put(AnnotationKey<T> key, T value) {
        annotations.put(key, value);
    }

    public <T> T get(AnnotationKey<T> key) {
        return this.tryGet(key).get();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> tryGet(AnnotationKey<T> key) {
        return Optional.ofNullable((T) this.annotations.get(key));
    }
}
