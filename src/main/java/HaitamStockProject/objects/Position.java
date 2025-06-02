package HaitamStockProject.objects;

public class Position {

    private final String symbol;

    public int getQuantity() {
        return quantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void addShares(int quantity) {
        this.quantity += quantity;
    }

    private int quantity;

    public Position(String symbol, int quantity) {
        this.symbol = symbol;
        this.quantity = quantity;
    }
}
