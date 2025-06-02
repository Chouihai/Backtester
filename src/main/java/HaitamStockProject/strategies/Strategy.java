package HaitamStockProject.strategies;

import HaitamStockProject.objects.Order;

import java.util.List;

public interface Strategy<T> {

    List<Order> roll(T inputs);
}
