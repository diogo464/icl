package icl.ast;

public class AstStr<T> extends AstNode<T> {
	public final String value;

	public AstStr(T annotation, String value) {
		super(annotation);
		this.value = value;
	}

	@Override
	public void accept(AstVisitor<T> visitor) {
		throw new RuntimeException("Trying to visit a string literal");
	}

}
