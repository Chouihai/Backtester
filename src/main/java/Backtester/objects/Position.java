package Backtester.objects;

import java.util.ArrayList;
import java.util.List;

public class Position {

    public enum Direction { LONG, SHORT, FLAT }

    private final List<Trade> trades;
    private Direction direction;
    private int quantity;

    public Position() {
        this.direction = Direction.FLAT;
        this.trades = new ArrayList<>();
        this.quantity = 0;
    }

    public void addTrade(Trade trade) {
        trades.add(trade);
    }

    public double netProfit() {
        List<Trade> closedTrades = trades.stream().filter(trade -> !trade.isOpen()).toList();
        double sum = 0;
        for (Trade trade: closedTrades) {
            sum += trade.profit();
        }
        return sum;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<Trade> getTrades() {
        return new ArrayList<>(trades);
    }
}
