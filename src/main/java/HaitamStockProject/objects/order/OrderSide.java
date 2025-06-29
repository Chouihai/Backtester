package HaitamStockProject.objects.order;

public enum OrderSide {
    BUY,
    SELL;

    public static OrderSide flip(OrderSide side) {
        if (side == BUY) return SELL;
        else return BUY;
    }
}

