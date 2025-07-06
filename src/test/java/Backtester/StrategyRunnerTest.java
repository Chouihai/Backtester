package Backtester;

import Backtester.caches.InMemoryOrderCache;
import Backtester.objects.Bar;
import Backtester.objects.order.Order;
import Backtester.objects.valueaccumulator.CrossoverDetector;
import Backtester.objects.valueaccumulator.key.CrossoverKey;
import Backtester.objects.valueaccumulator.key.SmaKey;
import Backtester.objects.valueaccumulator.SmaCalculator;
import Backtester.strategies.PositionManager;
import Backtester.strategies.StrategyRunner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

import Backtester.caches.BarCache;
import Backtester.caches.OrderCache;
import Backtester.caches.ValueAccumulatorCache;

public class StrategyRunnerTest {

    private final OrderCache orderCache = new InMemoryOrderCache();
    private final static List<Bar> bars = new ArrayList<>();
    private static BarCache barCache;
    private final ValueAccumulatorCache vaCache = new ValueAccumulatorCache();
    private Injector injector = Guice.createInjector(new MyModule());

    @BeforeAll
    static void setupOnce() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = StrategyRunnerTest.class.getResourceAsStream("/AAPL.JSON");
        JsonNode root = mapper.readTree(is);
        JsonNode results = root.get("results");

        int i = 0;
        for (JsonNode node : results) {
            long timestampMillis = node.get("t").asLong();
            double open = node.get("o").asDouble();
            double close = node.get("c").asDouble();

            LocalDate date = Instant.ofEpochMilli(timestampMillis)
                    .atZone(ZoneId.of("GMT"))
                    .toLocalDate();

            Bar value = new Bar(
                    i,
                    date,
                    open,
                    0.0, // high
                    0.0, // low
                    close,
                    0L
            );

            i++;
            bars.add(value);
        }
        barCache = new BarCache();
        barCache.loadCache(bars);
    }

    @Test
    void test_unchanged() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)
                
                if (sma20 > sma50)
                    createOrder("long", true, 10)
                if (sma50 > sma20)
                    createOrder("position1", false, 10)
                """;

        LocalDate currentDate = LocalDate.of(2024, 1, 2);
        List<Bar> initialValues = new ArrayList<>();
        // 50 days of flat prices, no crossover at start
        for (int i = 0; i < 50; i++) {
            initialValues.add(new Bar(i, currentDate, 0, 0,0,100,0));
            currentDate = currentDate.plusDays(1);
        }

        BarCache inMemoryBarCache = new BarCache();
        inMemoryBarCache.loadCache(initialValues);
        injector = Guice.createInjector(new MyModule(inMemoryBarCache));
        StrategyRunner runner = injector.getInstance(StrategyRunner.class);
        ValueAccumulatorCache vaCache = injector.getInstance(ValueAccumulatorCache.class);

        runner.initialize(script, new Bar(50, currentDate.plusDays(1), 0, 0,0,100,0));
        SmaCalculator va = (SmaCalculator) vaCache.getValueAccumulator(new SmaKey(20));
        assertEquals(100, va.getAverage());
        SmaCalculator va2 = (SmaCalculator) vaCache.getValueAccumulator(new SmaKey(50));
        assertEquals(100, va2.getAverage());
        assertEquals(0, orderCache.snapshot().size());
    }

    @Test
    void test_bullish() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)
                
                if (crossover(sma20, sma50))
                    createOrder("long", true, 10)
                if (crossover(sma50, sma20))
                    createOrder("position1", false, 10)
                """;

        LocalDate currentDate = LocalDate.of(2024, 1, 2);
        List<Bar> initialValues = new ArrayList<>();
        // 50 days of flat prices, no crossover at start
        for (int i = 0; i < 50; i++) {
            initialValues.add(new Bar(i, currentDate, 0, 0,0,100,0));
            currentDate = currentDate.plusDays(1);
        }

        BarCache inMemoryBarCache = new BarCache();
        inMemoryBarCache.loadCache(initialValues);
        injector = Guice.createInjector(new MyModule(inMemoryBarCache));
        StrategyRunner runner = injector.getInstance(StrategyRunner.class);
        ValueAccumulatorCache vaCache = injector.getInstance(ValueAccumulatorCache.class);

        // We initialize by setting sma20 lower than sma50
        runner.initialize(script, new Bar(50, LocalDate.of(2024, 3, 14), 0, 0,0,90,0));
        SmaCalculator sma20 = (SmaCalculator) vaCache.getValueAccumulator(new SmaKey(20));
        SmaCalculator sma50 = (SmaCalculator) vaCache.getValueAccumulator(new SmaKey(50));
        CrossoverDetector cd1 = (CrossoverDetector) vaCache.getValueAccumulator(new CrossoverKey(new SmaKey(20), new SmaKey(50)));
        CrossoverDetector cd2 = (CrossoverDetector) vaCache.getValueAccumulator(new CrossoverKey(new SmaKey(50), new SmaKey(20)));
        assertEquals(99.5, sma20.getAverage());
        assertEquals(99.8, sma50.getAverage());
        assertFalse(cd1.getValue()); // Should be false, no crossover can happen on initialization
        assertFalse(cd2.getValue()); // Should be false, no crossover can happen on initialization


        runner.roll(new Bar(51, LocalDate.of(2024, 3, 15), 0, 0,0,160,0));
        assertEquals(102.5, sma20.getAverage());
        assertEquals(101, sma50.getAverage());
        assertTrue(cd1.getValue()); // 20 crosses over 50, should be true
        assertFalse(cd2.getValue());


        assertEquals(1, orderCache.snapshot().size());

        runner.roll(new Bar(52, LocalDate.of(2024, 3, 15), 0, 0,0,150,0));
        assertEquals(105, sma20.getAverage()); // 20 still above 50
        assertEquals(102, sma50.getAverage());

        assertEquals(1, orderCache.snapshot().size());
    }

    @Test
    void irlSimulation() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)

                if (crossover(sma20, sma50))
                    createOrder("long", true, 1000)
                if (crossover(sma50, sma20))
                    createOrder("position1", false, 1000)
                """;

        BarCache inMemoryBarCache = new BarCache();

        injector = Guice.createInjector(new MyModule(inMemoryBarCache));

        StrategyRunner runner = injector.getInstance(StrategyRunner.class);
        PositionManager positionManager = injector.getInstance(PositionManager.class);

        inMemoryBarCache.loadCache(bars);

        runner.initialize(script, bars.get(50));

        int i = 51;
        while (i < bars.size()) {
            // First we check if we need to make any trades
            runner.roll(bars.get(i));
            i++;
        }
        assertEquals(8, orderCache.snapshot().size());
        List<Order> orders = new ArrayList<>(orderCache.snapshot().values());
        orders.sort(Comparator.comparing(Order::fillDate));
        assertEquals(LocalDate.of(2024, 5, 9), orders.get(0).fillDate());
        assertEquals(LocalDate.of(2024, 8, 21), orders.get(1).fillDate());
        assertEquals(LocalDate.of(2024, 8, 30), orders.get(2).fillDate());
        assertEquals(LocalDate.of(2024, 11, 20), orders.get(3).fillDate());
        assertEquals(LocalDate.of(2024, 12, 4), orders.get(4).fillDate());
        assertEquals(LocalDate.of(2025, 1, 28), orders.get(5).fillDate());
        assertEquals(LocalDate.of(2025, 3, 7), orders.get(6).fillDate());
        assertEquals(LocalDate.of(2025, 3, 18), orders.get(7).fillDate());

        assertEquals(0, positionManager.openTrades());
        assertEquals(4, positionManager.closedTrades());
        assertEquals(43_960.00, positionManager.grossProfit());
        assertEquals(35_100.00, positionManager.grossLoss());
        assertEquals(8_860.00, positionManager.netProfit());
    }

    @Test
    void irlSimulation2() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)

                if (crossover(sma20, sma50))
                    createOrder("long", true, 1000)
                if (crossover(sma50, sma20))
                    createOrder("position1", false, 500)
                """;

        BarCache inMemoryBarCache = new BarCache();
        inMemoryBarCache.loadCache(bars);
        injector = Guice.createInjector(new MyModule(inMemoryBarCache));
        StrategyRunner runner = injector.getInstance(StrategyRunner.class);
        PositionManager positionManager = injector.getInstance(PositionManager.class);
        runner.initialize(script, bars.get(50));

        int i = 51;
        while (i < bars.size()) {
            // First we check if we need to make any trades
            runner.roll(bars.get(i));
            i++;
        }
        assertEquals(8, orderCache.snapshot().size());

        assertEquals(2, positionManager.openTrades());
        assertEquals(4, positionManager.closedTrades());
        assertEquals(45_060.00, positionManager.grossProfit());
        assertEquals(8_015.00, positionManager.grossLoss());
        assertEquals(37_045.00, positionManager.netProfit());
        assertEquals(-59_820, positionManager.openPnL());
    }

    @Test
    void irlSimulation3() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)

                if (crossover(sma20, sma50))
                    createOrder("long", true, 500)
                    createOrder("long2", true, 500)
                if (crossover(sma50, sma20))
                    createOrder("position1", false, 1000)
                """;

        BarCache inMemoryBarCache = new BarCache();
        inMemoryBarCache.loadCache(bars);
        injector = Guice.createInjector(new MyModule(inMemoryBarCache));
        StrategyRunner runner = injector.getInstance(StrategyRunner.class);
        PositionManager positionManager = injector.getInstance(PositionManager.class);
        runner.initialize(script, bars.get(50));

        int i = 51;
        while (i < bars.size()) {
            // First we check if we need to make any trades
            runner.roll(bars.get(i));
            i++;
        }
        assertEquals(12, orderCache.snapshot().size());
        List<Order> orders = new ArrayList<>(orderCache.snapshot().values());
        orders.sort(Comparator.comparing(Order::fillDate));
        assertEquals(LocalDate.of(2024, 5, 9), orders.get(0).fillDate());
        assertEquals(LocalDate.of(2024, 5, 9), orders.get(1).fillDate());
        assertEquals(LocalDate.of(2024, 8, 21), orders.get(2).fillDate());
        assertEquals(LocalDate.of(2024, 8, 30), orders.get(3).fillDate());
        assertEquals(LocalDate.of(2024, 8, 30), orders.get(4).fillDate());
        assertEquals(LocalDate.of(2024, 11, 20), orders.get(5).fillDate());
        assertEquals(LocalDate.of(2024, 12, 4), orders.get(6).fillDate());
        assertEquals(LocalDate.of(2024, 12, 4), orders.get(7).fillDate());
        assertEquals(LocalDate.of(2025, 1, 28), orders.get(8).fillDate());
        assertEquals(LocalDate.of(2025, 3, 7), orders.get(9).fillDate());
        assertEquals(LocalDate.of(2025, 3, 7), orders.get(10).fillDate());
        assertEquals(LocalDate.of(2025, 3, 18), orders.get(11).fillDate());

        assertEquals(0, positionManager.openTrades());
        assertEquals(8, positionManager.closedTrades());
        assertEquals(43_960.00, positionManager.grossProfit());
        assertEquals(35_100.00, positionManager.grossLoss());
        assertEquals(8_860.00, positionManager.netProfit());
    }

    @Test
    void irlSimulation4() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)

                if (crossover(sma20, sma50))
                    createOrder("A", true, 600)
                    createOrder("B", true, 500)
                if (crossover(sma50, sma20))
                    createOrder("position1", false, 500)
                """;

        BarCache inMemoryBarCache = new BarCache();
        inMemoryBarCache.loadCache(bars);
        injector = Guice.createInjector(new MyModule(inMemoryBarCache));
        StrategyRunner runner = injector.getInstance(StrategyRunner.class);
        PositionManager positionManager = injector.getInstance(PositionManager.class);
        runner.initialize(script, bars.get(50));

        int i = 51;
        while (i < bars.size()) {
            // First we check if we need to make any trades
            runner.roll(bars.get(i));
            i++;
        }
        assertEquals(12, orderCache.snapshot().size());
        List<Order> orders = new ArrayList<>(orderCache.snapshot().values());
        orders.sort(Comparator.comparing(Order::fillDate));

        assertEquals(5, positionManager.openTrades());
        assertEquals(7, positionManager.closedTrades());
        assertEquals(49_823.00, positionManager.grossProfit());
        assertEquals(8_015.00, positionManager.grossLoss());
        assertEquals(41_808.00, positionManager.netProfit());
    }

    class MyModule extends AbstractModule {

        private final BarCache inMemoryBarCache;

        public MyModule() {
            this.inMemoryBarCache = StrategyRunnerTest.barCache;
        }

        public MyModule(BarCache barCache) {
            this.inMemoryBarCache = barCache;
        }

        @Override
        protected void configure() {
            bind(OrderCache.class).toInstance(orderCache);
            bind(ValueAccumulatorCache.class).toInstance(vaCache);
            bind(BarCache.class).toInstance(inMemoryBarCache);
        }
    }
}
