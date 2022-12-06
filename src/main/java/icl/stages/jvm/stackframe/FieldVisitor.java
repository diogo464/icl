package icl.stages.jvm.stackframe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import icl.ast.AstDecl;
import icl.ast.AstFn;
import icl.ast.AstScope;
import icl.ast.BaseAstVisitor;
import icl.stages.jvm.NameGenerator;
import icl.stages.jvm.TypeDescriptor;
import icl.stages.typecheck.TypeCheckStage;

// Visits AstNodes and gathers all variable declarations.
// Does not visit into scopes.
public class FieldVisitor extends BaseAstVisitor {

    private final NameGenerator nameGenerator;
    private final List<StackFrameField> fields;

    public FieldVisitor(NameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
        this.fields = new ArrayList<>();
    }

    public List<StackFrameField> getFields() {
        return Collections.unmodifiableList(List.copyOf(this.fields));
    }

    @Override
    public void acceptDecl(AstDecl node) {
        var name = node.name;
        var type = node.value.getAnnotation(TypeCheckStage.TYPE_KEY);
        var field = this.nameGenerator.generateVariableName();
        var descriptor = TypeDescriptor.fromValueType(type);
        this.fields.add(new StackFrameField(name, type, field, descriptor));
    }

    @Override
    public void acceptScope(AstScope scope) {
        // Dont visit into scopes
    }

    @Override
    public void acceptFn(AstFn fn) {
        // Dont visit into functions
    }
}
