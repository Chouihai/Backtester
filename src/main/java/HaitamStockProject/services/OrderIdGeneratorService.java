package HaitamStockProject.services;

import HaitamStockProject.objects.Order;

public interface OrderIdGeneratorService {

    String generateId(Order order);
}
