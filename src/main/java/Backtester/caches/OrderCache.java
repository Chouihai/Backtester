package Backtester.caches;

import Backtester.objects.order.Order;

import java.util.Map;
import java.util.Set;

/**
 * In memory cache
 */
public interface OrderCache {

    void addOrder(Order order);

    Order getOrder(int id);

    Set<Order> getOrders(String grouping);

    Map<Integer, Order> snapshot();

    void reset();
} 