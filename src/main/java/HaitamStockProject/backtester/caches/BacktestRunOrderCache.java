package HaitamStockProject.backtester.caches;

import HaitamStockProject.objects.Order;

import java.util.Map;

public interface BacktestRunOrderCache {

    void addOrder(Order order);

    Order getOrder(String orderId);

    Map<String, Order> snapshot();
}
