package icl.ast;

public class AstStr extends AstNode {
	public final String value;

	public AstStr(String value) {
		this.value = value;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptStr(this);
	}

}
