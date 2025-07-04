package HaitamStockProject.strategies;

import HaitamStockProject.backtester.caches.OrderCache;
import HaitamStockProject.objects.Bar;
import HaitamStockProject.objects.Position;
import HaitamStockProject.objects.Trade;
import HaitamStockProject.objects.order.*;
import com.google.inject.Inject;

import java.util.List;

public class PositionManager {

    private final OrderCache orderCache; // Orders will be filled immediately at the next open. Any order with status
    private final Position position;

    @Inject
    public PositionManager(OrderCache orderCache) {
        this.orderCache = orderCache;
        this.position = new Position();
    }

    // Ordering of whether we roll the PositionManager or script first will be important.
    public void roll(Bar bar) {
        List<Order> openMarketOrders = orderCache.snapshot().values().stream().filter(order -> order.status() == OrderStatus.OPEN && order.orderType() == OrderType.Market).toList();
        if (!openMarketOrders.isEmpty()) {
            for (Order openOrder: openMarketOrders) {
                Order filledOrder = openOrder.withFillPrice(bar.getOpen()).withOrderStatus(OrderStatus.FILLED);
                orderCache.addOrder(filledOrder);
                applyToPosition(filledOrder, bar);
            }
        }
    }

    public double netProfit() {
        return position.netProfit();
    }

    public double grossProfit() {
        return position.grossProfit();
    }

    public double grossLoss() {
        return position.grossLoss();
    }

    // TODO: Anytime anything new is open, we create a trade.
    private void applyToPosition(Order order, Bar bar) {
        Position.Direction dir = position.getDirection();
        switch (order.side()) {
            case BUY -> {
                if (dir == Position.Direction.FLAT) { // enter open position
                    position.setDirection(Position.Direction.LONG);
                    position.setQuantity(order.quantity());
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.Direction.LONG);
                    position.addTrade(newTrade);
                } else if (dir == Position.Direction.LONG) { // extending open position
                    int qty = position.getQuantity() + order.quantity();
                    position.setQuantity(qty);
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.Direction.LONG);
                    position.addTrade(newTrade);
                } else { // Position is short
                    List<Trade> sortedOpenTrades = position.getSortedOpenTrades();
                    if (order.quantity() < position.getQuantity()) { // no opening needed here, entire order quantity will be absorbed
                        closeOrderQuantityFromPosition(order, bar, sortedOpenTrades, Trade.Direction.SHORT);
                    } else if (position.getQuantity() == order.quantity()) { // no opening needed here either, entire order quantity should be absorbed
                        position.setDirection(Position.Direction.FLAT);
                        position.setQuantity(0);
                        for (Trade openTrade: sortedOpenTrades) {
                            if (openTrade.isLong()) continue;
                            openTrade.close(bar);
                        }
                    } else {
                        int leftover = order.quantity() - position.getQuantity(); // Closing position and entering new one!
                        for (Trade trade : sortedOpenTrades) {
                            if (trade.isLong()) continue;
                            trade.close(bar);
                        }
                        position.setDirection(Position.Direction.LONG);
                        position.setQuantity(leftover);
                        Trade newTrade = new Trade(bar, leftover, Trade.Direction.LONG);
                        position.addTrade(newTrade);
                    }
                }
            }
            case SELL -> {
                if (dir == Position.Direction.FLAT) { // opening short position
                    position.setDirection(Position.Direction.SHORT);
                    position.setQuantity(order.quantity());
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.Direction.SHORT);
                    position.addTrade(newTrade);
                } else if (dir == Position.Direction.SHORT) { // extending short position
                    int newQty = position.getQuantity() + order.quantity();
                    position.setQuantity(newQty);
                    Trade newTrade = new Trade(bar, order.quantity(), Trade.Direction.SHORT);
                    position.addTrade(newTrade);
                } else {
                    List<Trade> sortedOpenTrades = position.getSortedOpenTrades();
                    if (order.quantity() < position.getQuantity()) { // closing some of the long position
                        closeOrderQuantityFromPosition(order, bar, sortedOpenTrades, Trade.Direction.LONG);
                    } else if (order.quantity() == position.getQuantity()) {
                        position.setDirection(Position.Direction.FLAT);
                        position.setQuantity(0);
                        for (Trade openTrade: sortedOpenTrades) {
                            if (openTrade.isShort()) continue;
                            openTrade.close(bar);
                        }
                    } else {
                        int leftover = order.quantity() - position.getQuantity();
                        for (Trade trade : sortedOpenTrades) {
                            if (trade.isShort()) continue;
                            trade.close(bar);
                        }
                        position.setDirection(Position.Direction.SHORT);
                        position.setQuantity(leftover);
                        Trade newTrade = new Trade(bar, leftover, Trade.Direction.SHORT);
                        position.addTrade(newTrade);
                    }
                }
            }
        }
    }

    /**
     * For when an order is reverse of the position but its quantity is less than the position's
     */
    private void closeOrderQuantityFromPosition(Order order, Bar bar, List<Trade> sortedOpenTrades, Trade.Direction directionToClose) {
        int remainingToClose = order.quantity();
        position.setQuantity(position.getQuantity() - order.quantity());
        for (Trade trade : sortedOpenTrades) {
            if (remainingToClose <= 0) break;
            if (trade.getDirection() != directionToClose) continue;

            int openQty = trade.openQuantity();

            int qtyToClose = Math.min(openQty, remainingToClose);
            trade.setClosedQuantity(trade.getClosedQuantity() + qtyToClose);

            if (trade.openQuantity() == 0) {
                trade.close(bar);
            }

            remainingToClose -= qtyToClose;
        }
        if (remainingToClose > 0) throw new IllegalStateException("Tried to close more than open trades allowed.");
    }
}
