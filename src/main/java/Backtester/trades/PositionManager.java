package Backtester.trades;

import Backtester.caches.OrderCache;
import Backtester.objects.Bar;
import Backtester.objects.Position;
import Backtester.objects.Trade;
import Backtester.objects.order.Order;
import Backtester.objects.order.OrderSide;
import Backtester.objects.order.OrderStatus;
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

    // Equity and metrics tracking
    private double initialCapital = 100_000.0;
    private final List<Double> equitySeries = new ArrayList<>();
    private final List<Double> periodReturns = new ArrayList<>();
    private double peakEquity = Double.NaN;
    private double troughEquity = Double.NaN;
    private double maxDrawdownFraction = Double.NaN; // negative value (e.g., -0.25)
    private double maxRunUpFraction = Double.NaN;    // positive value (e.g., 0.30)

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
        updateEquityMetrics();
    }

    public double netProfit() {
        return position.netProfit();
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
            sum += openTrade.openPnL(currentBar);
        }
        return sum;
    }

    public double openPnl(Trade trade) {
        return trade.openPnL(currentBar);
    }

    public double maxDrawdown() {
        return maxDrawdownFraction;
    }

    public double maxRunUp() {
        return maxRunUpFraction;
    }

    public double sharpeRatio() {
        if (periodReturns.size() < 2) return Double.NaN;
        double mean = periodReturns.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
        double variance = 0.0;
        for (double r : periodReturns) {
            variance += Math.pow(r - mean, 2);
        }
        variance /= (periodReturns.size() - 1); // sample variance
        double stdDev = Math.sqrt(variance);
        if (stdDev == 0.0) return 0.0;
        // Assume daily bars; annualize with 252
        return (mean / stdDev) * Math.sqrt(252);
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

    public int entriesCount() {
        return position.getTrades().size();
    }

    public int winnersCount() {
        return (int) position.getTrades().stream()
                .filter(trade -> trade.isClosed() && trade.profit() > 0)
                .count();
    }

    public int losersCount() {
        return (int) position.getTrades().stream()
                .filter(trade -> trade.isClosed() && trade.profit() < 0)
                .count();
    }

    public Position getPosition() {
        return position;
    }

    public void reset() {
        this.position = new Position();
        this.orderCache.reset();
        this.currentBar = null;
        this.equitySeries.clear();
        this.periodReturns.clear();
        this.peakEquity = Double.NaN;
        this.troughEquity = Double.NaN;
        this.maxDrawdownFraction = Double.NaN;
        this.maxRunUpFraction = Double.NaN;
    }

    public void setInitialCapital(double initialCapital) {
        this.initialCapital = initialCapital;
    }

    private void updateEquityMetrics() {
        double realized = netProfit();
        double unrealized = openPnL();
        double equity = initialCapital + realized + unrealized;

        // Append equity and compute returns
        if (!equitySeries.isEmpty()) {
            double prev = equitySeries.get(equitySeries.size() - 1);
            if (prev > 0) {
                periodReturns.add((equity / prev) - 1.0);
            }
        }
        equitySeries.add(equity);

        // Init peak/trough
        if (Double.isNaN(peakEquity)) peakEquity = equity;
        if (Double.isNaN(troughEquity)) troughEquity = equity;

        // Update peak and trough
        if (equity > peakEquity) peakEquity = equity;
        if (equity < troughEquity) troughEquity = equity;

        // Compute current drawdown (negative fraction) and run-up (positive fraction)
        if (peakEquity > 0) {
            double drawdown = (equity - peakEquity) / peakEquity; // <= 0
            if (Double.isNaN(maxDrawdownFraction) || drawdown < maxDrawdownFraction) {
                maxDrawdownFraction = drawdown;
            }
        }
        if (troughEquity > 0) {
            double runup = (equity - troughEquity) / troughEquity; // >= 0
            if (Double.isNaN(maxRunUpFraction) || runup > maxRunUpFraction) {
                maxRunUpFraction = runup;
            }
        }
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
        return switch (order.orderType()) {
            case Market -> true;
            case Limit -> shouldFillLimitOrder(order, bar);
            case Stop -> shouldFillStopOrder(order, bar);
            case StopLimit -> shouldFillLimitOrder(order, bar) && shouldFillStopOrder(order, bar);
        };
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
        return switch (order.orderType()) {
            case Market -> bar.open;
            case Limit, StopLimit -> calculateLimitFillPrice(order, bar);
            case Stop -> calculateStopFillPrice(order, bar);
        };
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
