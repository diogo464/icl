package icl.ast;

import java.util.Optional;

import icl.ValueType;

public class AstDecl extends AstNode {
	public final String name;
	public final AstNode value;
	public final boolean mutable;
	public final Optional<ValueType> type;

	public AstDecl(String name, AstNode value, boolean mutable, Optional<ValueType> type) {
		this.name = name;
		this.value = value;
		this.mutable = mutable;
		this.type = type;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.acceptDecl(this);
	}

}
