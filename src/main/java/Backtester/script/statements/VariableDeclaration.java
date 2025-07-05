package Backtester.script.statements;

import Backtester.script.statements.expressions.Expression;

public class VariableDeclaration extends Statement {
    public final String name;
    public final Expression initializer;

    public VariableDeclaration(String name, Expression initializer) {
        this.name = name;
        this.initializer = initializer;
    }
}