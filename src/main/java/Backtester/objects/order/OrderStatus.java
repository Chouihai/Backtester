package Backtester.objects.order;

public enum OrderStatus {
    OPEN,        // Order that hasn't been filled yet
    FILLED,      // Order has been completely filled
    CANCELLED,   // Order was explicitly cancelled before fill
    EXPIRED      // Order was never filled and reached due date (applies to stop/limit orders)
}
