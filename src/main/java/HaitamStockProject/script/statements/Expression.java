package HaitamStockProject.script.statements;

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
}

