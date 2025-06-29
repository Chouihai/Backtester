package HaitamStockProject.objects.order;

public enum OrderStatus {
    SUBMITTED,     // Limit or stop order waiting to be filled
    FILLED,      // Order has been completely filled
    CANCELLED,   // Order was explicitly cancelled before fill
    EXPIRED      // Order was never filled and expired due to time-based logic
}
