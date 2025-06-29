package HaitamStockProject.backtester.caches;

import HaitamStockProject.objects.order.Order;

import java.util.Map;
import java.util.Set;

/**
 * In memory cache, does not read anything from a database
 */
public interface OrderCache {

    void addOrder(Order order);

    Order getOrder(int id);

    Set<Order> getOrders(String grouping);

    Map<Integer, Order> snapshot();
}
