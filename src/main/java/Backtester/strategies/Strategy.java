package Backtester.strategies;

import Backtester.objects.order.Order;

import java.util.List;

public interface Strategy<T> {

    List<Order> roll(T inputs);
}