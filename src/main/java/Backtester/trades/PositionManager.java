package Backtester.trades;

import Backtester.caches.OrderCache;
import Backtester.objects.Bar;
import Backtester.objects.Position;
import Backtester.objects.Trade;
import Backtester.objects.order.Order;
import Backtester.objects.order.OrderSide;
import Backtester.objects.order.OrderStatus;
import Backtester.objects.order.OrderType;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Singleton
public class PositionManager {

    private final OrderCache orderCache; // Orders will be filled immediately at the next open. Any order with status
    private Position position;
    private Bar currentBar;
    private static final Comparator<Trade> TRADE_ORDERING = Comparator.comparing(Trade::getEntryBarDate).thenComparing(Trade::getOrderId);

    @Inject
    public PositionManager(OrderCache orderCache) {
        this.orderCache = orderCache;
        this.position = new Position();
    }

    // Ordering of whether we roll the PositionManager or script first will be important.
    public void roll(Bar bar) {
        this.currentBar = bar;
        List<Order> openOrders = orderCache.snapshot().values().stream()
                .filter(order -> order.status() == OrderStatus.OPEN)
                .toList();
        
        if (!openOrders.isEmpty()) {
            for (Order openOrder: openOrders) {
                if (shouldFillOrder(openOrder, bar)) {
                    double fillPrice = calculateFillPrice(openOrder, bar);
                    Order filledOrder = openOrder.withFillPrice(fillPrice)
                                               .withOrderStatus(OrderStatus.FILLED)
                                               .withFillDate(bar.date);
                    orderCache.addOrder(filledOrder);
                    applyToPosition(filledOrder, bar);
                }
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

    public List<Trade> getSortedOpenTrades() {
        return position.getTrades().stream()
                .filter(trade -> trade.getExit().isEmpty())
                .sorted(TRADE_ORDERING).toList();
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

    public double openPnl(Trade trade) {
        double entryValue = trade.entry.open * trade.getQuantity();
        double closeValue = currentBar.open * trade.getQuantity();
        int sign = trade.isLong() ? 1 : -1;
        return sign * (closeValue - entryValue);
    }

    public double maxDrawdown() {
        return Double.NaN; // TODO
    }

    public double maxRunUp() {
        return Double.NaN; // TODO
    }

    public double sharpeRatio() {
        return Double.NaN; // TODO
    }

    public int openTrades() {
        return position.getTrades().stream().filter(Trade::isOpen).toList().size();
    }

    public List<Trade> allTrades() {
        return position.getTrades();
    }

    public int closedTrades() {
        return position.getTrades().stream().filter(Trade::isClosed).toList().size();
    }

    public Position getPosition() {
        return position;
    }

    public void reset() {
        this.position = new Position();
        this.orderCache.reset();
        this.currentBar = null;
    }

    private void applyToPosition(Order order, Bar bar) {
        Position.Direction dir = position.getDirection();
        switch (order.side()) {
            case BUY -> {
                if (dir == Position.Direction.FLAT) { // enter open position
                    position.setDirection(Position.Direction.LONG);
                    position.setQuantity(order.quantity());
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.TradeDirection.LONG, order.label());
                    position.addTrade(newTrade);
                } else if (dir == Position.Direction.LONG) { // extending open position
                    int qty = position.getQuantity() + order.quantity();
                    position.setQuantity(qty);
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.TradeDirection.LONG, order.label());
                    position.addTrade(newTrade);
                } else { // Position is short
                    List<Trade> sortedOpenTrades = getSortedOpenTrades();
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
                        Trade newTrade = new Trade(bar, leftover, Trade.TradeDirection.LONG, order.label());
                        position.addTrade(newTrade);
                    }
                }
            }
            case SELL -> {
                if (dir == Position.Direction.FLAT) { // opening short position
                    position.setDirection(Position.Direction.SHORT);
                    position.setQuantity(order.quantity());
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.TradeDirection.SHORT, order.label());
                    position.addTrade(newTrade);
                } else if (dir == Position.Direction.SHORT) { // extending short position
                    int newQty = position.getQuantity() + order.quantity();
                    position.setQuantity(newQty);
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.TradeDirection.SHORT, order.label());
                    position.addTrade(newTrade);
                } else {
                    List<Trade> sortedOpenTrades = getSortedOpenTrades();
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
                        Trade newTrade = new Trade(bar, leftover, Trade.TradeDirection.SHORT, order.label());
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

    private boolean shouldFillOrder(Order order, Bar bar) {
        switch (order.orderType()) {
            case Market:
                return true; 
            case Limit:
                return shouldFillLimitOrder(order, bar);
            case Stop:
                return shouldFillStopOrder(order, bar);
            default:
                return false;
        }
    }

    private boolean shouldFillLimitOrder(Order order, Bar bar) {
        if (order.side() == OrderSide.BUY) return bar.low <= order.limitPrice();
        else return bar.high >= order.limitPrice();
    }

    private boolean shouldFillStopOrder(Order order, Bar bar) {
        if (order.side() == OrderSide.BUY) return bar.high >= order.stopPrice();
        else return bar.low <= order.stopPrice();
    }

    private double calculateFillPrice(Order order, Bar bar) {
        switch (order.orderType()) {
            case Market:
                return bar.open;
            case Limit:
                return calculateLimitFillPrice(order, bar);
            case Stop:
                return calculateStopFillPrice(order, bar);
            default:
                return bar.open;
        }
    }

    private double calculateLimitFillPrice(Order order, Bar bar) {
        if (order.side() == OrderSide.BUY) return Math.min(order.limitPrice(), bar.open);
        else return Math.max(order.limitPrice(), bar.open);
    }

    private double calculateStopFillPrice(Order order, Bar bar) {
        if (order.side() == OrderSide.BUY) return Math.max(order.stopPrice(), bar.open);
        else return Math.min(order.stopPrice(), bar.open);
    }
}
