package HaitamStockProject.script.statements.expressions;

import HaitamStockProject.script.tokens.Token;

public class BinaryExpression extends Expression {
    public final Expression left;
    public final Token operator;
    public final Expression right;

    public BinaryExpression(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public boolean isBinary() {
        return true;
    }
}
