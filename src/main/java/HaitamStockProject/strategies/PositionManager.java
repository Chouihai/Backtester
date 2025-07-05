package HaitamStockProject.strategies;

import HaitamStockProject.backtester.caches.OrderCache;
import HaitamStockProject.objects.Bar;
import HaitamStockProject.objects.Position;
import HaitamStockProject.objects.Trade;
import HaitamStockProject.objects.order.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class PositionManager {

    private final OrderCache orderCache; // Orders will be filled immediately at the next open. Any order with status
    private final Position position;
    private Bar currentBar;

    @Inject
    public PositionManager(OrderCache orderCache) {
        this.orderCache = orderCache;
        this.position = new Position();
    }

    // Ordering of whether we roll the PositionManager or script first will be important.
    public void roll(Bar bar) {
        this.currentBar = bar;
        List<Order> openMarketOrders = orderCache.snapshot().values().stream().filter(order -> order.status() == OrderStatus.OPEN && order.orderType() == OrderType.Market).toList();
        if (!openMarketOrders.isEmpty()) {
            for (Order openOrder: openMarketOrders) {
                Order filledOrder = openOrder.withFillPrice(bar.open).withOrderStatus(OrderStatus.FILLED);
                orderCache.addOrder(filledOrder);
                applyToPosition(filledOrder, bar);
            }
        }
    }

    public double netProfit() {
        List<Trade> closedTrades = position.getTrades().stream().filter(trade -> !trade.isOpen()).toList();
        double sum = 0;
        for (Trade trade: closedTrades) {
            sum += trade.profit();
        }
        return sum;
    }

    public double grossProfit() {
        List<Trade> closedTrades = position.getTrades().stream().filter(trade -> !trade.isOpen()).toList();
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
        List<Trade> closedTrades = position.getTrades().stream().filter(trade -> !trade.isOpen()).toList();
        double sum = 0;
        for (Trade trade: closedTrades) {
            double profit = trade.profit();
            if (profit < 0) {
                sum += profit;
            }
        }
        return Math.abs(sum);
    }

    public double openPnL() {
        List<Trade> openTrades = new ArrayList<>(position.getTrades().stream().filter(Trade::isOpen).toList());
        double sum = 0;
        for (Trade openTrade: openTrades) {
            double entryValue = openTrade.entry.open * openTrade.getQuantity();
            double closeValue = currentBar.open * openTrade.getQuantity();
            double pnl = closeValue - entryValue;
            sum += pnl;
        }
        return sum;
    }

    public int openTrades() {
        return position.getTrades().stream().filter(Trade::isOpen).toList().size();
    }

    public int closedTrades() {
        return position.getTrades().stream().filter(Trade::isClosed).toList().size();
    }

    private void applyToPosition(Order order, Bar bar) {
        Position.Direction dir = position.getDirection();
        switch (order.side()) {
            case BUY -> {
                if (dir == Position.Direction.FLAT) { // enter open position
                    position.setDirection(Position.Direction.LONG);
                    position.setQuantity(order.quantity());
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.TradeDirection.LONG);
                    position.addTrade(newTrade);
                } else if (dir == Position.Direction.LONG) { // extending open position
                    int qty = position.getQuantity() + order.quantity();
                    position.setQuantity(qty);
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.TradeDirection.LONG);
                    position.addTrade(newTrade);
                } else { // Position is short
                    List<Trade> sortedOpenTrades = position.getSortedOpenTrades();
                    if (order.quantity() < position.getQuantity()) { // no opening needed here, entire order quantity will be absorbed
                        closeOrderQuantityFromPosition(order, bar, sortedOpenTrades, Trade.TradeDirection.SHORT);
                    } else if (position.getQuantity() == order.quantity()) { // no opening needed here either, entire order quantity should be absorbed
                        position.setDirection(Position.Direction.FLAT);
                        position.setQuantity(0);
                        for (Trade openTrade: sortedOpenTrades) {
                            openTrade.close(bar);
                        }
                    } else {
                        int leftover = order.quantity() - position.getQuantity(); // Closing position and entering new one!
                        for (Trade trade : sortedOpenTrades) {
                            trade.close(bar);
                        }
                        position.setDirection(Position.Direction.LONG);
                        position.setQuantity(leftover);
                        Trade newTrade = new Trade(bar, leftover, Trade.TradeDirection.LONG);
                        position.addTrade(newTrade);
                    }
                }
            }
            case SELL -> {
                if (dir == Position.Direction.FLAT) { // opening short position
                    position.setDirection(Position.Direction.SHORT);
                    position.setQuantity(order.quantity());
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.TradeDirection.SHORT);
                    position.addTrade(newTrade);
                } else if (dir == Position.Direction.SHORT) { // extending short position
                    int newQty = position.getQuantity() + order.quantity();
                    position.setQuantity(newQty);
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.TradeDirection.SHORT);
                    position.addTrade(newTrade);
                } else {
                    List<Trade> sortedOpenTrades = position.getSortedOpenTrades();
                    if (order.quantity() < position.getQuantity()) {
                        closeOrderQuantityFromPosition(order, bar, sortedOpenTrades, Trade.TradeDirection.LONG);
                    } else if (order.quantity() == position.getQuantity()) {
                        position.setDirection(Position.Direction.FLAT);
                        position.setQuantity(0);
                        for (Trade openTrade: sortedOpenTrades) {
                            openTrade.close(bar);
                        }
                    } else {
                        int leftover = order.quantity() - position.getQuantity();
                        for (Trade trade : sortedOpenTrades) {
                            trade.close(bar);
                        }
                        position.setDirection(Position.Direction.SHORT);
                        position.setQuantity(leftover);
                        Trade newTrade = new Trade(bar, leftover, Trade.TradeDirection.SHORT);
                        position.addTrade(newTrade);
                    }
                }
            }
        }
    }

    /**
     * For when an order is reverse of the position but its quantity is less than the position's
     */
    private void closeOrderQuantityFromPosition(Order order, Bar bar, List<Trade> sortedOpenTrades, Trade.TradeDirection tradeDirectionToClose) {
        int remainingToClose = order.quantity();
        position.setQuantity(position.getQuantity() - order.quantity());
        for (Trade trade : sortedOpenTrades) {
            if (remainingToClose <= 0) break;
            if (trade.getDirection() != tradeDirectionToClose) continue;

            int qtyToClose = Math.min(trade.getQuantity(), remainingToClose);

            if (qtyToClose == trade.getQuantity()) trade.close(bar);
            else {
                Trade remainingOpenTrade =  trade.partialClose(bar, qtyToClose);
                this.position.addTrade(remainingOpenTrade);
            }
            remainingToClose -= qtyToClose;
        }
        if (remainingToClose > 0) throw new IllegalStateException("Tried to close more than open trades allowed.");
    }
}
