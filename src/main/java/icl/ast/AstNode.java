package icl.ast;

import java.util.Optional;

public abstract class AstNode {
	private final Annotations annotations;

	public AstNode() {
		this.annotations = new Annotations();
	}

	public <T> void annotate(AnnotationKey<T> key, T value) {
		annotations.put(key, value);
	}

	public <T> T getAnnotation(AnnotationKey<T> key) {
		return annotations.get(key);
	}

	public <T> Optional<T> tryGetAnnotation(AnnotationKey<T> key) {
		return annotations.tryGet(key);
	}

	public abstract void accept(AstVisitor visitor);
}
