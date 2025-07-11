package Backtester.caches;

import Backtester.objects.order.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemoryOrderCache implements OrderCache {

    private Map<Integer, Order> orderMap;

    public InMemoryOrderCache() {
        this.orderMap = new HashMap<>();
    }

    public InMemoryOrderCache(Map<Integer, Order> orderMap) {
        this.orderMap = orderMap;
    }

    public void addOrder(Order order) {
        orderMap.put(order.id(), order);
    }

    public Set<Order> getOrders(String group) {
        return orderMap.values().stream().filter(order -> Objects.equals(order.label(), group)).collect(Collectors.toSet());
    }

    public Order getOrder(int id) {
        return orderMap.get(id); // Make optional later
    }

    public Map<Integer, Order> snapshot() {
        return orderMap;
    }

    public void reset() {
        orderMap = new HashMap<>();
    }
} 