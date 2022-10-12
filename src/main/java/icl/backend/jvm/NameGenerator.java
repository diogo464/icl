package icl.backend.jvm;

class NameGenerator {
	private int stackframes;
	private int variables;

	public NameGenerator() {
		this.stackframes = 0;
		this.variables = 0;
	}

	public String generateStackFrameName() {
		return "stackframe_" + this.stackframes++;
	}

	public String generateVariableName() {
		return "var_" + this.variables++;
	}
}
