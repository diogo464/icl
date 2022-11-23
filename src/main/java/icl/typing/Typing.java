package icl.typing;

/*-
 *	Type checker rulesfor the AST.
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
public class Typing {
}
