package HaitamStockProject.objects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Trade> getSortedOpenTrades() {
        return trades.stream().filter(trade -> trade.getExit().isEmpty()).sorted(Comparator.comparing(Trade::getEntryBarDate)).collect(Collectors.toList());
    }

    public double netProfit() {
        List<Trade> closedTrades = trades.stream().filter(trade -> !trade.isOpen()).toList();
        double sum = 0;
        for (Trade trade: closedTrades) {
            sum += trade.profit();
        }
        return sum;
    }

    public double grossProfit() {
        List<Trade> closedTrades = trades.stream().filter(trade -> !trade.isOpen()).toList();
        double sum = 0;
        for (Trade trade: closedTrades) {
            double profit = trade.profit();
            if (profit > 0) {
                sum += profit;
            }
        }
        return sum;
    }
    public double grossLoss() {
        List<Trade> closedTrades = trades.stream().filter(trade -> !trade.isOpen()).toList();
        double sum = 0;
        for (Trade trade: closedTrades) {
            double profit = trade.profit();
            if (profit < 0) {
                sum += profit;
            }
        }
        return Math.abs(sum);
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
}
