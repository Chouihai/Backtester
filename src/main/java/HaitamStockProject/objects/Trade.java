package HaitamStockProject.objects;

import java.time.LocalDate;
import java.util.Optional;

public class Trade {
    public enum Direction { LONG, SHORT }

    final int quantity;
    final Bar entry;

    private int closedQuantity;
    private Direction direction;
    private Optional<Bar> exit;

    public Trade(Bar entry, int quantity, Direction direction) {
        this.entry = entry;
        this.quantity = quantity;
        this.direction = direction;
        this.exit = Optional.empty();
    }

    public void close(Bar exit) {
        this.exit = Optional.of(exit);
        this.closedQuantity = quantity;
    }

    public Optional<Bar> getExit() {
        return exit;
    }

    public Direction getDirection() {
        return direction;
    }

    public LocalDate getEntryBarDate() {
        return entry.getDate();
    }

    public int getClosedQuantity() {
        return closedQuantity;
    }

    public void setClosedQuantity(int closedQuantity) {
        this.closedQuantity = closedQuantity;
    }

    public boolean isLong() {
        return direction == Direction.LONG;
    }

    public boolean isShort() {
        return direction == Direction.SHORT;
    }

    public int openQuantity() {
        return quantity - closedQuantity;
    }

    public boolean isOpen() {
        return exit.isEmpty();
    }

    public double profit() {
        if (exit.isEmpty()) return 0;
        else {
            double entryValue = entry.getOpen() * quantity;
            double exitValue = exit.get().getOpen() * quantity;
            return exitValue - entryValue;
        }
    }

    // TODO: Convert the Bar fields to BigDecimals rounded to 2 decimal places
}
