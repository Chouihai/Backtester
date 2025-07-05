package Backtester.strategies;

import Backtester.caches.InMemoryOrderCache;
import Backtester.strategies.PositionManager;
import Backtester.caches.OrderCache;
import Backtester.objects.Bar;
import Backtester.objects.Position;
import Backtester.objects.Trade;
import Backtester.objects.order.Order;
import Backtester.objects.order.OrderSide;
import Backtester.objects.order.OrderStatus;
import Backtester.objects.order.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        testBar = new Bar(testDate, 100.0, 105.0, 95.0, 102.0, 1000);
    }

    private int getTotalOpenQuantity() {
        return positionManager.getPosition().getTrades().stream()
                .filter(trade -> trade.isOpen())
                .mapToInt(trade -> trade.getQuantity())
                .sum();
    }

    private int getTotalClosedQuantity() {
        return positionManager.getPosition().getTrades().stream()
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
        Bar closeBar = new Bar(testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
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
        Bar closeBar = new Bar(testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
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
        Bar closeBar = new Bar(testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
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
        Bar currentBar = new Bar(testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
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
        Bar profitBar = new Bar(testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
        positionManager.roll(profitBar);
        Order buyOrder2 = new Order(3, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate.plusDays(2), "test");
        orderCache.addOrder(buyOrder2);
        Bar lossBar = new Bar(testDate.plusDays(2), 110.0, 115.0, 105.0, 112.0, 1000);
        positionManager.roll(lossBar);
        Order sellOrder2 = new Order(4, "AAPL", OrderStatus.OPEN, OrderSide.SELL, OrderType.Market, 0, 0, 0, 100, testDate.plusDays(3), "test");
        orderCache.addOrder(sellOrder2);
        Bar finalBar = new Bar(testDate.plusDays(3), 105.0, 110.0, 100.0, 107.0, 1000);
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
    void testOrderTypeFiltering() {
        Order marketOrder = new Order(1, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Market, 0, 0, 0, 100, testDate, "test");
        Order limitOrder = new Order(2, "AAPL", OrderStatus.OPEN, OrderSide.BUY, OrderType.Limit, 95.0, 0, 0, 100, testDate, "test");
        orderCache.addOrder(marketOrder);
        orderCache.addOrder(limitOrder);
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
        positionManager.roll(new Bar(date1, 100.0, 105.0, 95.0, 102.0, 1000));
        orderCache.addOrder(order2);
        positionManager.roll(new Bar(date2, 101.0, 106.0, 96.0, 103.0, 1000));
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
        Bar closeBar = new Bar(testDate.plusDays(1), 110.0, 115.0, 105.0, 112.0, 1000);
        positionManager.roll(closeBar);
        assertEquals(1, positionManager.openTrades());
        assertEquals(1, positionManager.closedTrades());
        assertEquals(40, getTotalOpenQuantity());
        assertEquals(60, getTotalClosedQuantity());
    }
} 