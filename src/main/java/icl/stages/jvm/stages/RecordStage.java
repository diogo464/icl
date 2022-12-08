package icl.stages.jvm.stages;

import icl.ast.AnnotationKey;
import icl.ast.AstNode;
import icl.ast.AstRecord;
import icl.ast.BaseAstVisitor;
import icl.pipeline.PipelineStage;
import icl.stages.jvm.Compiler;
import icl.stages.jvm.Context;
import icl.stages.jvm.JvmUtils;
import icl.stages.jvm.struct.Record;
import icl.stages.typecheck.TypeCheckStage;

/*-
 * This stage does 3 thing:
 * 1. Annotates each AstRecord node with the record that it represents.
 * 2. Compiles each unique record.
 * 3. Associates each record ValueType with its typename.
 * 
 * Annotations:
 *  AstRecord:
 *      - RECORD_KEY: The record that the node represents.
 */
public class RecordStage implements PipelineStage<AstNode, AstNode> {
    public static final AnnotationKey<Record> RECORD_KEY = new AnnotationKey<>("jvm-record");

    private final Context context;

    private static class Visitor extends BaseAstVisitor {
        private final Context context;

        public Visitor(Context context) {
            this.context = context;
        }

        @Override
        public void acceptRecord(AstRecord node) {
            node.fields.values().forEach(field -> field.accept(this));

            var type = node.getAnnotation(TypeCheckStage.TYPE_KEY);
            var rtype = type.getRecord();
            var typename = this.context.getValueTypeTypename(type);
            var descriptor = JvmUtils.descriptorFromTypename(typename);
            var record = new Record(rtype, typename, descriptor);
            var compiled = Compiler.compile(this.context, record);

            node.annotate(RECORD_KEY, record);
            this.context.addCompiledClass(compiled);
        }
    }

    public RecordStage(Context context) {
        this.context = context;
    }

    @Override
    public AstNode process(AstNode input) {
        var visitor = new Visitor(this.context);
        input.accept(visitor);
        return input;
    }
}
