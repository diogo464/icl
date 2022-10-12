package icl.backend.jvm;

import java.util.List;

public class JvmOutput {
	private final List<JvmClass> _classes;

	JvmOutput(List<JvmClass> classes) {
		this._classes = classes;
	}

	public Iterable<JvmClass> classes() {
		return this._classes;
	}
}
