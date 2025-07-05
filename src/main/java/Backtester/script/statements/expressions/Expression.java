package Backtester.script.statements.expressions;

public abstract class Expression {

    public boolean isLiteral() {
        return false;
    }

    public boolean isFunctionCall() {
        return false;
    }

    public boolean isIdentifier() {
        return false;
    }

    public boolean isBinary() {
        return false;
    }

    public boolean isValueAccumulatorLiteral() {
        return false;
    }

    public boolean isSeries() {
        return false;
    }
 }

