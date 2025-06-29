package HaitamStockProject.objects;

/**
 * Represents the entire position of a security with its stats and everything
 */
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
