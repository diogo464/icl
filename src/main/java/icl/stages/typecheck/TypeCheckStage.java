package icl.stages.typecheck;

import icl.Environment;
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
 *  Field:
 *     The field must exist in the record.
 *     The type of a field is the type of the field in the record.
 */
public class TypeCheckStage implements PipelineStage<AstNode, AstNode> {
    public static final AnnotationKey<ValueType> TYPE_KEY = new AnnotationKey<>("type");

    @Override
    public AstNode process(AstNode input) {
        var env = new Environment<ValueType>();
        var output = check(env, input);
        return output;
    }

    static AstNode check(Environment<ValueType> env, AstNode node) {
        var visitor = new Visitor(env);
        node.accept(visitor);
        return node;
    }
}
