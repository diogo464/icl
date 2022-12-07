package icl.stages.jvm;

public class NameGenerator {
	private int stackframes;
	private int variables;
	private int references;

	public NameGenerator() {
		this.stackframes = 0;
		this.variables = 0;
		this.references = 0;
	}

	public String generateStackFrameName() {
		return "stackframe_" + this.stackframes++;
	}

	public String generateVariableName() {
		return "var_" + this.variables++;
	}

	public String generateReferenceName() {
		return "ref_" + this.references++;
	}
}
