package Backtester.objects;

import java.time.LocalDate;
import java.util.Optional;

public class Trade {
    public enum TradeDirection { LONG, SHORT }
    private int quantity;
    public final Bar entry;
    private final TradeDirection tradeDirection;
    private Optional<Bar> exit;
    public final String orderId;

    public Trade(Bar entry, int quantity, TradeDirection tradeDirection, String orderId) {
        this.entry = entry;
        this.quantity = quantity;
        this.tradeDirection = tradeDirection;
        this.exit = Optional.empty();
        this.orderId = orderId;
    }

    public void close(Bar exit) {
        this.exit = Optional.of(exit);
    }

    /**
     * Closes this trade with the partial quantity. Returns the remaining quantity as a new open trade.
     */
    public Trade partialClose(Bar exit, int quantity) {
        if (quantity > this.quantity) throw new IllegalArgumentException("Can't partial close more than or equal the full quantity of the trade.");
        this.exit = Optional.of(exit);
        int oldQuantity = this.quantity;
        this.quantity = quantity;
        return new Trade(entry, oldQuantity - quantity, tradeDirection, orderId);
    }

    public int getQuantity() {
        return quantity;
    }

    public Optional<Bar> getExit() {
        return exit;
    }

    public TradeDirection getDirection() {
        return tradeDirection;
    }

    public LocalDate getEntryBarDate() {
        return entry.date;
    }

    public boolean isLong() {
        return tradeDirection == TradeDirection.LONG;
    }

    public boolean isShort() {
        return tradeDirection == TradeDirection.SHORT;
    }

    public boolean isOpen() {
        return exit.isEmpty();
    }

    public boolean isClosed() {
        return !isOpen();
    }

    public double profit() {
        if (exit.isEmpty()) return 0;
        else {
            double entryValue = entry.open * quantity;
            double exitValue = exit.get().open * quantity;
            int sign = isLong() ? 1 : -1;
            return sign * exitValue - entryValue;
        }
    }

    public double openPnL(Bar currentBar) {
        if (!exit.isEmpty()) return 0;
        double entryValue = entry.open * quantity;
        double currentValue = currentBar.open * quantity;
        int sign = isLong() ? 1 : -1;
        return sign * currentValue - entryValue;
    }

    public String getOrderId() {
        return orderId;
    }

}
