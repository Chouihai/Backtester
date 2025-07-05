package Backtester.script.statements;

import Backtester.script.statements.expressions.Expression;

import java.util.List;

public class IfStatement extends Statement {
    public final Expression condition;
    public final List<Statement> body;

    public IfStatement(Expression condition, List<Statement> body) {
        this.condition = condition;
        this.body = body;
    }
}
