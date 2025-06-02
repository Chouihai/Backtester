package HaitamStockProject.backtester;

import HaitamStockProject.objects.Order;
import HaitamStockProject.objects.Position;

import java.util.List;

public class BacktesterResult {

    private final List<Order> trades;
    private final double finalBalance;

    public Position getPosition() {
        return position;
    }

    public double getFinalBalance() {
        return finalBalance;
    }

    public List<Order> getTrades() {
        return trades;
    }

    private final Position position;

    public BacktesterResult(List<Order> trades, double finalBalance, Position position) {
        this.trades = trades;
        this.finalBalance = finalBalance;
        this.position = position;
    }
}
