package icl.ast;

public class AstAssign extends AstNode {
	public final String name;
	public final AstNode value;

	public AstAssign(String name, AstNode value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptAssign(this);
	}
}
