package icl.stages.interpretor.value;

import java.util.List;

import icl.Environment;
import icl.ValueType;
import icl.ast.AstFn;
import icl.ast.AstNode;
import icl.stages.interpretor.InterpretorStage;

public class FnValue extends Value {
    Environment<Value> env;
    List<AstFn.Arg> args;
    AstNode body;

    FnValue(ValueType type, Environment<Value> environment, List<AstFn.Arg> args, AstNode body) {
        super(type);
        this.env = environment;
        this.args = args;
        this.body = body;

        assert type.getKind() == ValueType.Kind.Function;
    }

    public Value evaluate(List<AstNode> arguments) {
        var eval_env = this.env.beginScope();
        for (var i = 0; i < arguments.size(); ++i) {
            var farg = this.args.get(i);
            var arg = arguments.get(i);
            var argvalue = InterpretorStage.interpret(eval_env, arg);
            eval_env.define(farg.name, argvalue);
        }
        return InterpretorStage.interpret(eval_env, this.body);
    }
}
