package icl.mir;

import icl.Environment;
import icl.ast.AstNode;
import icl.hir.Hir;
import icl.type.ValueType;

/*-
 *	Type rules
 *
 *	Assign:
 * 		The left side of the assignment
 *
 *	BinOp:
 *		Both sides of the binary operator must have the same type.
 *		AllowedTypes:
 *			Number: 	ADD, SUB, MUL, DIV, CMP, GT, GTE, LT, LTE
 *			Boolean:	CMP, LAND, LOR
 *
 *	Call:
 *		function:  Must be a Function with correct argument types 
 *
 * 	If:
 * 		Conditional.condition: Must be Boolean
 * 		All conditionals expressions and fallthrough must have the same type.
 *
 * 	Loop:
 * 		condtion: Must be Boolean
 *
 * 	UnaryOp:
 * 		AllowedTypes:
 * 			Number: 	POS, NEG
 * 			Boolean:	LNOT
 * 			Reference:	DEREF
 *		The type of a Deref unary op is the target type of the reference.
 *
 */
public class Mir {
	public final Hir hir;
	public final ValueType type;

	Mir(Hir hir, ValueType type) {
		this.hir = hir;
		this.type = type;
	}

	@Override
	public String toString() {
		return "Type = " + this.type;
	}

	public static AstNode<Mir> toMir(AstNode<Hir> node) {
		var env = new Environment<ValueType>();
		return lower(env, node);
	}

	static AstNode<Mir> lower(Environment<ValueType> env, AstNode<Hir> node) {
		var visitor = new Visitor(env);
		node.accept(visitor);
		return visitor.lowered;
	}

}
