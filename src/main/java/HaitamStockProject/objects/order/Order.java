package HaitamStockProject.objects.order;

import java.time.LocalDate;

// Orders don't have a settle date. They are made at close and executed at open.
public record Order(int id, String symbol, OrderStatus status, OrderSide side, OrderType orderType, double price, int signedQuantity, LocalDate tradeDate, String group) {

    // Need to check if it's an open order (hasn't been executed yet, pending)
    // Need a stop/limit price, and order type
    // Statuses

    public Order withGroup(String group) {
        return new Order(id, symbol, status, side, orderType, price, signedQuantity, tradeDate, group);
    }
}

