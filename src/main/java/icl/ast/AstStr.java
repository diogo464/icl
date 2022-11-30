package icl.ast;

public class AstStr extends AstNode {
	public final String value;

	public AstStr(String value) {
		this.value = value;
	}

	@Override
	public void accept(AstVisitor visitor) {
		throw new RuntimeException("Trying to visit a string literal");
	}

}
