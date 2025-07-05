package Backtester.script.statements;

import Backtester.script.statements.expressions.Expression;

public class ExpressionStatement extends Statement {
    public final Expression expression;

    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }
}
