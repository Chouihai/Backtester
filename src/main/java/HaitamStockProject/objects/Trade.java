package HaitamStockProject.objects;

import java.time.LocalDate;
import java.util.Optional;

public class Trade {
    public enum TradeDirection { LONG, SHORT }
    private int quantity;
    public final Bar entry;
    private final TradeDirection tradeDirection;
    private Optional<Bar> exit;

    public Trade(Bar entry, int quantity, TradeDirection tradeDirection) {
        this.entry = entry;
        this.quantity = quantity;
        this.tradeDirection = tradeDirection;
        this.exit = Optional.empty();
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
        return new Trade(entry, oldQuantity - quantity, tradeDirection);
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
            return exitValue - entryValue;
        }
    }
}
