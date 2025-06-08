package HaitamStockProject.script.statements;

public class Literal extends Expression {
    public final Object value;

    public Literal(Object value) {
        this.value = value;
    }

    @Override
    public boolean isLiteral() {
        return true;
    }
}