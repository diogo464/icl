package icl.stages.typecheck;

import icl.ValueType;
import icl.ast.AnnotationKey;
import icl.ast.AstNode;
import icl.pipeline.PipelineStage;

/*-
 *	Type rules
 *
 *	Assign:
 * 		The left side of the assignment
 *
 *	BinOp:
 *		Both sides of the binary operator must have the same type.
 *		AllowedTypes:
 *			Number: 	ADD, SUB, MUL, DIV, IDIV, CMP, GT, GTE, LT, LTE
 *			Boolean:	CMP, LAND, LOR
 *          String:     ADD, CMP
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
 * 			Boolean:
 	LNOT
 * 			Reference:	DEREF
 *		The type of a Deref unary op is the target type of the reference.
 *
 *  Field:
 *     The field must exist in the record.
 *     The type of a field is the type of the field in the record.
 * 
 * Builtin:
 *  All builtins evaluate to the return type, it is invalid to use a builtin without the proper arguments.
 *      SIN     - fn(Number) -> Number
 *      COS     - fn(Number) -> Number
 *      TAN     - fn(Number) -> Number
 *      SQRT    - fn(Number) -> Number
 *      ABS     - fn(Number) -> Number
 *      POW     - fn(Number, Number) -> Number
 *      MAX     - fn(Number, Number) -> Number
 *      MIN     - fn(Number, Number) -> Number
 *      PI      - fn() -> Number
 * ----------------
 * 
 * Annotations:
 *  AstNode:
 *      - TYPE_KEY: The type of the node, all custom types have been resolved to builtin types.
 */
public class TypeCheckStage implements PipelineStage<AstNode, AstNode> {
    public static final AnnotationKey<ValueType> TYPE_KEY = new AnnotationKey<>("type");

    @Override
    public AstNode process(AstNode input) {
        var env = new TypeCheckEnv();
        var output = check(env, input);
        return output;
    }

    static AstNode check(TypeCheckEnv env, AstNode node) {
        var visitor = new Visitor(env);
        node.accept(visitor);
        return node;
    }
}
