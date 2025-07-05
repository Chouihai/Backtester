package Backtester.script.statements.expressions;

public class Identifier extends Expression {
    public final String name;

    public Identifier(String name) {
        this.name = name;
    }

    @Override
    public boolean isIdentifier() {
        return true;
    }
}
