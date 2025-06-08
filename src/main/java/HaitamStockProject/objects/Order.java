package HaitamStockProject.objects;

import java.time.LocalDate;

public record Order(String orderId, String symbol, double price, int signedQuantity, LocalDate tradeDate,
                    LocalDate settleDate) {

    public Order withNewId(String orderId) {
        return new Order(orderId, symbol, price, signedQuantity, tradeDate, settleDate);
    }
}
