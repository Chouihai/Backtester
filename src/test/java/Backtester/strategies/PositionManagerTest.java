package Backtester.strategies;

import Backtester.caches.InMemoryOrderCache;
import Backtester.caches.OrderCache;
import Backtester.objects.Bar;
import Backtester.objects.order.Order;
import Backtester.objects.order.OrderSide;
import Backtester.objects.order.OrderStatus;
import Backtester.objects.order.OrderType;
import Backtester.trades.PositionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionManagerTest {

    private PositionManager positionManager;
    private OrderCache orderCache;
    private LocalDate testDate;
    private Bar testBar;

    @BeforeEach
    void setUp() {
        orderCache = new InMemoryOrderCache();
        positionManager = new PositionManager(orderCache);
        testDate = LocalDate.of(2024, 1, 1);
        testBar = new Bar(1, testDate, 100.0, 105.0, 95.0, 102.0, 1000);
    }

    private int getTotalOpenQuantity() {
        return positionManager.allTrades().stream()
                .filter(trade -> trade.isOpen())
                .mapToInt(trade -> trade.getQuantity())
                .sum();
    }

    private int getTotalClosedQuantity() {
        return positionManager.allTrades().stream()
                .filter(trade -> !trade.isOpen())
                .mapToInt(trade -> trade.getQuantity())
                .sum();
    }

    @Test
    void testInitialState() {
        assertEquals(0, positionManager.netProfit());
        assertEquals(0, positionManager.grossProfit());
        assertEquals(0, positionManager.grossLoss());
        assertEquals(0, positionManager.openPnL());
        assertEquals(0, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(0, getTotalOpenQuantity());
        assertEquals(0, getTotalClosedQuantity());
    }

    @Test
    void testEnterLongPosition() {
        Order buyOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyOrder);
        positionManager.roll(testBar);
        assertEquals(1, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(100, getTotalOpenQuantity());
    }

    @Test
    void testEnterShortPosition() {
        Order sellOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(sellOrder);
        positionManager.roll(testBar);
        assertEquals(1, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(100, getTotalOpenQuantity());
    }

    @Test
    void testExtendLongPosition() {
        Order buyOrder1 = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyOrder1);
        positionManager.roll(testBar);
        Order buyOrder2 = new Order(2, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 50, testDate, "test");
        orderCache.addOrder(buyOrder2);
        positionManager.roll(testBar);
        assertEquals(2, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(150, getTotalOpenQuantity());
    }

    @Test
    void testCloseLongPositionCompletely() {
        Order buyOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyOrder);
        positionManager.roll(testBar);
        Order sellOrder = new Order(2, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(sellOrder);
        Bar closeBar = new Bar(2, testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
        positionManager.roll(closeBar);
        assertEquals(0, positionManager.openTrades());
        assertEquals(1, positionManager.closedTrades());
        assertEquals(0, getTotalOpenQuantity());
        assertEquals(100, getTotalClosedQuantity());
        double expectedProfit = (110.0 - 100.0) * 100;
        assertEquals(expectedProfit, positionManager.netProfit());
    }

    @Test
    void testCloseLongPositionPartially() {
        Order buyOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyOrder);
        positionManager.roll(testBar);
        Order sellOrder = new Order(2, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Market, 0, 0, 0, 60, testDate, "test");
        orderCache.addOrder(sellOrder);
        Bar closeBar = new Bar(2, testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
        positionManager.roll(closeBar);
        assertEquals(1, positionManager.openTrades());
        assertEquals(1, positionManager.closedTrades());
        assertEquals(40, getTotalOpenQuantity());
        assertEquals(60, getTotalClosedQuantity());
        double expectedProfit = (110.0 - 100.0) * 60;
        assertEquals(expectedProfit, positionManager.netProfit());
    }

    @Test
    void testReversePositionLongToShort() {
        Order buyOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyOrder);
        positionManager.roll(testBar);
        Order sellOrder = new Order(2, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Market, 0, 0, 0, 150, testDate, "test");
        orderCache.addOrder(sellOrder);
        Bar closeBar = new Bar(2, testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
        positionManager.roll(closeBar);
        assertEquals(1, positionManager.openTrades());
        assertEquals(1, positionManager.closedTrades());
        assertEquals(50, getTotalOpenQuantity());
        assertEquals(100, getTotalClosedQuantity());
    }

    @Test
    void testOpenPnLCalculation() {
        Order buyOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyOrder);
        positionManager.roll(testBar);
        Bar currentBar = new Bar(2, testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
        positionManager.roll(currentBar);
        double expectedOpenPnL = (110.0 - 100.0) * 100;
        assertEquals(expectedOpenPnL, positionManager.openPnL());
    }

    @Test
    void testGrossProfitAndLoss() {
        Order buyOrder1 = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyOrder1);
        positionManager.roll(testBar);
        Order sellOrder1 = new Order(2, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Market, 0, 0, 0, 100, testDate.plusDays(1), "test");
        orderCache.addOrder(sellOrder1);
        Bar profitBar = new Bar(2, testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
        positionManager.roll(profitBar);
        Order buyOrder2 = new Order(3, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate.plusDays(2), "test");
        orderCache.addOrder(buyOrder2);
        Bar lossBar = new Bar(2, testDate.plusDays(2), 110.0, 115.0, 105.0, 112.0, 1000);
        positionManager.roll(lossBar);
        Order sellOrder2 = new Order(4, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Market, 0, 0, 0, 100, testDate.plusDays(3), "test");
        orderCache.addOrder(sellOrder2);
        Bar finalBar = new Bar(2, testDate.plusDays(3), 105.0, 110.0, 100.0, 107.0, 1000);
        positionManager.roll(finalBar);
        double expectedNetProfit = (110.0 - 100.0) * 100 + (105.0 - 110.0) * 100;
        double expectedGrossProfit = (110.0 - 100.0) * 100;
        double expectedGrossLoss = Math.abs((105.0 - 110.0) * 100);
        assertEquals(expectedNetProfit, positionManager.netProfit());
        assertEquals(expectedGrossProfit, positionManager.grossProfit());
        assertEquals(expectedGrossLoss, positionManager.grossLoss());
    }

    @Test
    void testMultipleOrdersInSameRoll() {
        Order buyOrder1 = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 50, testDate, "test");
        Order buyOrder2 = new Order(2, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 50, testDate, "test");
        orderCache.addOrder(buyOrder1);
        orderCache.addOrder(buyOrder2);
        positionManager.roll(testBar);
        assertEquals(2, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(100, getTotalOpenQuantity());
    }

    @Test
    void testOrderStatusFiltering() {
        Order openOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        Order filledOrder = new Order(2, "AAPL", OrderStatus.FILLED, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(openOrder);
        orderCache.addOrder(filledOrder);
        positionManager.roll(testBar);
        assertEquals(1, positionManager.openTrades());
        assertEquals(100, getTotalOpenQuantity());
    }

    @Test
    void testTradeOrdering() {
        LocalDate date1 = testDate;
        LocalDate date2 = testDate.plusDays(1);
        Order order1 = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, date1, "test");
        Order order2 = new Order(2, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, date2, "test");
        orderCache.addOrder(order1);
        positionManager.roll(new Bar(2, date1, 100.0, 105.0, 95.0, 102.0, 1000));
        orderCache.addOrder(order2);
        positionManager.roll(new Bar(3, date2, 101.0, 106.0, 96.0, 103.0, 1000));
        // Just check that both trades are open and quantities are correct
        assertEquals(2, positionManager.openTrades());
        assertEquals(200, getTotalOpenQuantity());
    }

    @Test
    void testPartialCloseWithRemainingTrade() {
        Order buyOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyOrder);
        positionManager.roll(testBar);
        Order sellOrder = new Order(2, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Market, 0, 0, 0, 60, testDate, "test");
        orderCache.addOrder(sellOrder);
        Bar closeBar = new Bar(2, testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
        positionManager.roll(closeBar);
        assertEquals(1, positionManager.openTrades());
        assertEquals(1, positionManager.closedTrades());
        assertEquals(40, getTotalOpenQuantity());
        assertEquals(60, getTotalClosedQuantity());
    }

    @Test
    void testBuyLimitOrderFilledWhenPriceFalls() {
        // Buy limit at 95, current bar low is 94 (should fill)
        Order buyLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Limit, 95.0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyLimitOrder);
        Bar barWithLowPrice = new Bar(2, testDate.plusDays(1), 100.0, 105.0, 94.0, 102.0, 1000);
        positionManager.roll(barWithLowPrice);
        assertEquals(1, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(100, getTotalOpenQuantity());
    }

    @Test
    void testBuyLimitOrderNotFilledWhenPriceAbove() {
        // Buy limit at 95, current bar low is 96 (should not fill)
        Order buyLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Limit, 95.0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyLimitOrder);
        Bar barWithHighPrice = new Bar(2, testDate.plusDays(1), 100.0, 105.0, 96.0, 102.0, 1000);
        positionManager.roll(barWithHighPrice);
        assertEquals(0, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(0, getTotalOpenQuantity());
    }

    @Test
    void testSellLimitOrderFilledWhenPriceRises() {
        // Sell limit at 105, current bar high is 106 (should fill)
        Order sellLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Limit, 105.0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(sellLimitOrder);
        Bar barWithHighPrice = new Bar(2, testDate.plusDays(1), 100.0, 106.0, 95.0, 102.0, 1000);
        positionManager.roll(barWithHighPrice);
        assertEquals(1, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(100, getTotalOpenQuantity());
    }

    @Test
    void testSellLimitOrderNotFilledWhenPriceBelow() {
        // Sell limit at 105, current bar high is 104 (should not fill)
        Order sellLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Limit, 105.0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(sellLimitOrder);
        Bar barWithLowPrice = new Bar(2, testDate.plusDays(1), 100.0, 104.0, 95.0, 102.0, 1000);
        positionManager.roll(barWithLowPrice);
        assertEquals(0, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(0, getTotalOpenQuantity());
    }

    @Test
    void testBuyLimitOrderFillPriceCalculation() {
        // Buy limit at 95, bar opens at 100, low at 94
        // Should fill at 95 (limit price, not the better price of 94)
        Order buyLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Limit, 95.0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyLimitOrder);
        Bar barWithLowPrice = new Bar(2, testDate.plusDays(1), 100.0, 105.0, 94.0, 102.0, 1000);
        positionManager.roll(barWithLowPrice);

        // Check that the order was filled at the limit price
        var filledOrders = orderCache.snapshot().values().stream()
                .filter(order -> order.status() == OrderStatus.FILLED)
                .toList();
        assertEquals(1, filledOrders.size());
        assertEquals(95.0, filledOrders.get(0).fillPrice());
    }

    @Test
    void testSellLimitOrderFillPriceCalculation() {
        // Sell limit at 105, bar opens at 100, high at 106
        // Should fill at 105 (limit price, not the better price of 106)
        Order sellLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Limit, 105.0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(sellLimitOrder);
        Bar barWithHighPrice = new Bar(2, testDate.plusDays(1), 100.0, 106.0, 95.0, 102.0, 1000);
        positionManager.roll(barWithHighPrice);

        // Check that the order was filled at the limit price
        var filledOrders = orderCache.snapshot().values().stream()
                .filter(order -> order.status() == OrderStatus.FILLED)
                .toList();
        assertEquals(1, filledOrders.size());
        assertEquals(105.0, filledOrders.get(0).fillPrice());
    }

    @Test
    void testBuyStopOrderFilledWhenPriceRises() {
        // Buy stop at 105, current bar high is 106 (should fill)
        Order buyStopOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Stop, 0, 105.0, 0, 100, testDate, "test");
        orderCache.addOrder(buyStopOrder);
        Bar barWithHighPrice = new Bar(2, testDate.plusDays(1), 100.0, 106.0, 95.0, 102.0, 1000);
        positionManager.roll(barWithHighPrice);
        assertEquals(1, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(100, getTotalOpenQuantity());
    }

    @Test
    void testBuyStopOrderNotFilledWhenPriceBelow() {
        // Buy stop at 105, current bar high is 104 (should not fill)
        Order buyStopOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Stop, 0, 105.0, 0, 100, testDate, "test");
        orderCache.addOrder(buyStopOrder);
        Bar barWithLowPrice = new Bar(2, testDate.plusDays(1), 100.0, 104.0, 95.0, 102.0, 1000);
        positionManager.roll(barWithLowPrice);
        assertEquals(0, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(0, getTotalOpenQuantity());
    }

    @Test
    void testSellStopOrderFilledWhenPriceFalls() {
        // Sell stop at 95, current bar low is 94 (should fill)
        Order sellStopOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Stop, 0, 95.0, 0, 100, testDate, "test");
        orderCache.addOrder(sellStopOrder);
        Bar barWithLowPrice = new Bar(2, testDate.plusDays(1), 100.0, 105.0, 94.0, 102.0, 1000);
        positionManager.roll(barWithLowPrice);
        assertEquals(1, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(100, getTotalOpenQuantity());
    }

    @Test
    void testSellStopOrderNotFilledWhenPriceAbove() {
        // Sell stop at 95, current bar low is 96 (should not fill)
        Order sellStopOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Stop, 0, 95.0, 0, 100, testDate, "test");
        orderCache.addOrder(sellStopOrder);
        Bar barWithHighPrice = new Bar(2, testDate.plusDays(1), 100.0, 105.0, 96.0, 102.0, 1000);
        positionManager.roll(barWithHighPrice);
        assertEquals(0, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(0, getTotalOpenQuantity());
    }

    @Test
    void testBuyStopOrderFillPriceCalculation() {
        // Buy stop at 105, bar opens at 100, high at 106
        // Should fill at 105 (stop price, not the worse price of 106)
        Order buyStopOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Stop, 0, 105.0, 0, 100, testDate, "test");
        orderCache.addOrder(buyStopOrder);
        Bar barWithHighPrice = new Bar(2, testDate.plusDays(1), 100.0, 106.0, 95.0, 102.0, 1000);
        positionManager.roll(barWithHighPrice);

        // Check that the order was filled at the stop price
        var filledOrders = orderCache.snapshot().values().stream()
                .filter(order -> order.status() == OrderStatus.FILLED)
                .toList();
        assertEquals(1, filledOrders.size());
        assertEquals(105.0, filledOrders.get(0).fillPrice());
    }

    @Test
    void testSellStopOrderFillPriceCalculation() {
        // Sell stop at 95, bar opens at 100, low at 94
        // Should fill at 95 (stop price, not the worse price of 94)
        Order sellStopOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Stop, 0, 95.0, 0, 100, testDate, "test");
        orderCache.addOrder(sellStopOrder);
        Bar barWithLowPrice = new Bar(2, testDate.plusDays(1), 100.0, 105.0, 94.0, 102.0, 1000);
        positionManager.roll(barWithLowPrice);

        // Check that the order was filled at the stop price
        var filledOrders = orderCache.snapshot().values().stream()
                .filter(order -> order.status() == OrderStatus.FILLED)
                .toList();
        assertEquals(1, filledOrders.size());
        assertEquals(95.0, filledOrders.get(0).fillPrice());
    }

    @Test
    void testMixedOrderTypesInSameRoll() {
        // Market order should always fill
        Order marketOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 50, testDate, "test");
        // Limit order should fill (limit at 95, bar low at 94)
        Order limitOrder = new Order(2, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Limit, 95.0, 0, 0, 50, testDate, "test");
        // Stop order should not fill (stop at 105, bar high at 104)
        Order stopOrder = new Order(3, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Stop, 0, 105.0, 0, 50, testDate, "test");

        orderCache.addOrder(marketOrder);
        orderCache.addOrder(limitOrder);
        orderCache.addOrder(stopOrder);

        Bar mixedBar = new Bar(2, testDate.plusDays(1), 100.0, 104.0, 94.0, 102.0, 1000);
        positionManager.roll(mixedBar);

        // Only market and limit orders should fill
        assertEquals(2, positionManager.openTrades());
        assertEquals(0, positionManager.closedTrades());
        assertEquals(100, getTotalOpenQuantity());

        // Check that stop order remains open
        var openOrders = orderCache.snapshot().values().stream()
                .filter(order -> order.status() == OrderStatus.OPEN)
                .toList();
        assertEquals(1, openOrders.size());
        assertEquals(OrderType.Stop, openOrders.get(0).orderType());
    }

    @Test
    void testLimitOrderFillsOnExactPrice() {
        // Buy limit at exactly 95, bar low is exactly 95 (should fill)
        Order buyLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Limit, 95.0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(buyLimitOrder);
        Bar exactBar = new Bar(2, testDate.plusDays(1), 100.0, 105.0, 95.0, 102.0, 1000);
        positionManager.roll(exactBar);
        assertEquals(1, positionManager.openTrades());
        assertEquals(100, getTotalOpenQuantity());
    }

    @Test
    void testStopOrderFillsOnExactPrice() {
        // Sell stop at exactly 95, bar low is exactly 95 (should fill)
        Order sellStopOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Stop, 0, 95.0, 0, 100, testDate, "test");
        orderCache.addOrder(sellStopOrder);
        Bar exactBar = new Bar(2, testDate.plusDays(1), 100.0, 105.0, 95.0, 102.0, 1000);
        positionManager.roll(exactBar);
        assertEquals(1, positionManager.openTrades());
        assertEquals(100, getTotalOpenQuantity());
    }

    @Test
    void testBuyStopLimitOrderFillsOnlyIfStopAndLimitTriggered() {
        // Buy stop-limit: stop at 105, limit at 95
        Order stopLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.StopLimit, 95.0, 105.0, 0, 100, testDate, "test");
        orderCache.addOrder(stopLimitOrder);
        // Bar triggers stop (high >= 105) and limit (low <= 95)
        Bar bar = new Bar(2, testDate.plusDays(1), 100.0, 106.0, 94.0, 102.0, 1000);
        positionManager.roll(bar);
        var filledOrders = orderCache.snapshot().values().stream().filter(order -> order.status() == OrderStatus.FILLED).toList();
        assertEquals(1, filledOrders.size());
        assertEquals(OrderType.StopLimit, filledOrders.get(0).orderType());
        assertEquals(95.0, filledOrders.get(0).fillPrice());
    }

    @Test
    void testBuyStopLimitOrderDoesNotFillIfOnlyStopTriggered() {
        // Buy stop-limit: stop at 105, limit at 95
        Order stopLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.StopLimit, 95.0, 105.0, 0, 100, testDate, "test");
        orderCache.addOrder(stopLimitOrder);
        // Bar triggers stop (high >= 105) but not limit (low > 95)
        Bar bar = new Bar(2, testDate.plusDays(1), 100.0, 106.0, 96.0, 102.0, 1000);
        positionManager.roll(bar);
        var filledOrders = orderCache.snapshot().values().stream().filter(order -> order.status() == OrderStatus.FILLED).toList();
        assertEquals(0, filledOrders.size());
    }
    @Test
    void testBuyStopLimitOrderDoesNotFillIfOnlyLimitTriggered() {
        // Buy stop-limit: stop at 105, limit at 95
        Order stopLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.StopLimit, 95.0, 105.0, 0, 100, testDate, "test");
        orderCache.addOrder(stopLimitOrder);
        // Bar triggers limit (low <= 95) but not stop (high < 105)
        Bar bar = new Bar(2, testDate.plusDays(1), 100.0, 104.0, 94.0, 102.0, 1000);
        positionManager.roll(bar);
        var filledOrders = orderCache.snapshot().values().stream().filter(order -> order.status() == OrderStatus.FILLED).toList();
        assertEquals(0, filledOrders.size());
    }

    @Test
    void testSellStopLimitOrderFillsOnlyIfStopAndLimitTriggered() {
        // Sell stop-limit: stop at 95, limit at 105
        Order stopLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.StopLimit, 105.0, 95.0, 0, 100, testDate, "test");
        orderCache.addOrder(stopLimitOrder);
        // Bar triggers stop (low <= 95) and limit (high >= 105)
        Bar bar = new Bar(2, testDate.plusDays(1), 100.0, 106.0, 94.0, 102.0, 1000);
        positionManager.roll(bar);
        var filledOrders = orderCache.snapshot().values().stream().filter(order -> order.status() == OrderStatus.FILLED).toList();
        assertEquals(1, filledOrders.size());
        assertEquals(OrderType.StopLimit, filledOrders.get(0).orderType());
        assertEquals(105.0, filledOrders.get(0).fillPrice());
    }

    @Test
    void testSellStopLimitOrderDoesNotFillIfOnlyStopTriggered() {
        // Sell stop-limit: stop at 95, limit at 105
        Order stopLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.StopLimit, 105.0, 95.0, 0, 100, testDate, "test");
        orderCache.addOrder(stopLimitOrder);
        // Bar triggers stop (low <= 95) but not limit (high < 105)
        Bar bar = new Bar(2, testDate.plusDays(1), 100.0, 104.0, 94.0, 102.0, 1000);
        positionManager.roll(bar);
        var filledOrders = orderCache.snapshot().values().stream().filter(order -> order.status() == OrderStatus.FILLED).toList();
        assertEquals(0, filledOrders.size());
    }

    @Test
    void testSellStopLimitOrderDoesNotFillIfOnlyLimitTriggered() {
        // Sell stop-limit: stop at 95, limit at 105
        Order stopLimitOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.StopLimit, 105.0, 95.0, 0, 100, testDate, "test");
        orderCache.addOrder(stopLimitOrder);
        // Bar triggers limit (high >= 105) but not stop (low > 95)
        Bar bar = new Bar(2, testDate.plusDays(1), 100.0, 106.0, 96.0, 102.0, 1000);
        positionManager.roll(bar);
        var filledOrders = orderCache.snapshot().values().stream().filter(order -> order.status() == OrderStatus.FILLED).toList();
        assertEquals(0, filledOrders.size());
    }
} 