package HaitamStockProject.objects.order;

import java.time.LocalDate;

// An order's P&L is the price * quantity on the day it was opened - price * quantity on day it was closed (or current date if it's still open)
public record Order(int id,
                    String symbol,
                    OrderStatus status,
                    OrderSide side,
                    OrderType orderType,
                    double limitPrice,
                    double stopPrice,
                    double fillPrice,
                    int quantity,
                    LocalDate tradeDate,
                    String group) {

    public Order withGroup(String group) {
        return new Order(id, symbol, status, side, orderType, limitPrice, stopPrice, fillPrice, quantity, tradeDate, group);
    }

    public Order withFillPrice(double price) {
        return new Order(id, symbol, status, side, orderType, limitPrice, stopPrice, price, quantity, tradeDate, group);
    }

    public Order withOrderStatus(OrderStatus status) {
        return new Order(id, symbol, status, side, orderType, limitPrice, stopPrice, fillPrice, quantity, tradeDate, group);
    }
}

