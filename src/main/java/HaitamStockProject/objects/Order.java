package HaitamStockProject.objects;

import java.time.LocalDate;

public class Order {

    private final String symbol;
    private final double price;
    private final int signedQuantity;
    private final LocalDate tradeDate;
    private final LocalDate settleDate;

    public LocalDate getSettleDate() {
        return settleDate;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public int getSignedQuantity() {
        return signedQuantity;
    }

    public double getPrice() {
        return price;
    }

    public String getSymbol() {
        return symbol;
    }

    public Order(String symbol, double price, int signedQuantity, LocalDate tradeDate, LocalDate settleDate) {
        this.symbol = symbol;
        this.price = price;
        this.signedQuantity = signedQuantity;
        this.tradeDate = tradeDate;
        this.settleDate = settleDate;
    }
}
