package HaitamStockProject;

import HaitamStockProject.backtester.caches.*;
import HaitamStockProject.caches.InMemoryOrderCache;
import HaitamStockProject.objects.Bar;
import HaitamStockProject.objects.order.Order;
import HaitamStockProject.objects.valueaccumulator.CrossoverDetector;
import HaitamStockProject.objects.valueaccumulator.key.CrossoverKey;
import HaitamStockProject.objects.valueaccumulator.key.SmaKey;
import HaitamStockProject.services.BusinessDayService;
import HaitamStockProject.services.MockBusinessDayService;
import HaitamStockProject.objects.valueaccumulator.SmaCalculator;
import HaitamStockProject.strategies.PositionManager;
import HaitamStockProject.strategies.StrategyRunner;
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

public class StrategyRunnerTest {


    private static BusinessDayService businessDayService;
    private static final Set<LocalDate> holidays = new HashSet<>();
    private final LocalDate startDate = LocalDate.of(2024, 1, 1);
    private final OrderCache orderCache = new InMemoryOrderCache();
    private final static Map<LocalDate, Bar> bars = new HashMap<>();
    private static BarCache barCache;
    private final ValueAccumulatorCache vaCache = new ValueAccumulatorCache();
    private Injector injector = Guice.createInjector(new MyModule());

    @BeforeAll
    static void setupOnce() throws IOException {
        // ---- 2024 Holidays ----
        holidays.add(LocalDate.of(2024, 1, 1));  // New Year's Day
        holidays.add(LocalDate.of(2024, 1, 15)); // MLK Jr. Day
        holidays.add(LocalDate.of(2024, 2, 19)); // Presidents’ Day
        holidays.add(LocalDate.of(2024, 3, 29)); // Good Friday
        holidays.add(LocalDate.of(2024, 5, 27)); // Memorial Day
        holidays.add(LocalDate.of(2024, 6, 19)); // Juneteenth
        holidays.add(LocalDate.of(2024, 7, 4));  // Independence Day
        holidays.add(LocalDate.of(2024, 9, 2));  // Labor Day
        holidays.add(LocalDate.of(2024, 11, 28)); // Thanksgiving
        holidays.add(LocalDate.of(2024, 12, 25)); // Christmas

        // ---- 2025 Holidays ----
        holidays.add(LocalDate.of(2025, 1, 1));  // New Year's Day
        holidays.add(LocalDate.of(2025, 1, 9));  // Jimmy Carter died, RIP da goat
        holidays.add(LocalDate.of(2025, 1, 20)); // MLK Jr. Day
        holidays.add(LocalDate.of(2025, 2, 17)); // Presidents’ Day
        holidays.add(LocalDate.of(2025, 4, 18)); // Good Friday
        businessDayService = new MockBusinessDayService(holidays);

        ObjectMapper mapper = new ObjectMapper();
        InputStream is = StrategyRunnerTest.class.getResourceAsStream("/AAPL.JSON");
        JsonNode root = mapper.readTree(is);
        JsonNode results = root.get("results");

        for (JsonNode node : results) {
            long timestampMillis = node.get("t").asLong();
            double open = node.get("o").asDouble();
            double close = node.get("c").asDouble();

            LocalDate date = Instant.ofEpochMilli(timestampMillis)
                    .atZone(ZoneId.of("GMT"))
                    .toLocalDate();

            Bar value = new Bar(
                    date,
                    open,
                    0.0, // high
                    0.0, // low
                    close,
                    0L
            );

            bars.put(date, value);
        }
        barCache = new InMemoryBarCache(businessDayService);
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
        Map<LocalDate, Bar> initialValues = new HashMap<>();
        // 50 days of flat prices, no crossover at start
        for (int i = 0; i < 50; i++) {
            initialValues.put(currentDate, new Bar(currentDate, 0, 0,0,100,0));
            currentDate = businessDayService.nextBusinessDay(currentDate);
        }

        BarCache inMemoryBarCache = new InMemoryBarCache(businessDayService);
        inMemoryBarCache.loadCache(initialValues);
        injector = Guice.createInjector(new MyModule(inMemoryBarCache));
        StrategyRunner runner = injector.getInstance(StrategyRunner.class);
        ValueAccumulatorCache vaCache = injector.getInstance(ValueAccumulatorCache.class);

        runner.initialize(script, "AAPL", new Bar(LocalDate.of(2024, 3, 14), 0, 0,0,100,0));
//        runner.roll(new Bar(LocalDate.of(2024, 3, 15), 0, 0,0,100,0));
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
        Map<LocalDate, Bar> initialValues = new HashMap<>();
        // 50 days of flat prices, no crossover at start
        for (int i = 0; i < 50; i++) {
            initialValues.put(currentDate, new Bar(currentDate, 0, 0,0,100,0));
            currentDate = businessDayService.nextBusinessDay(currentDate);
        }
        BarCache inMemoryBarCache = new InMemoryBarCache(businessDayService);
        inMemoryBarCache.loadCache(initialValues);
        injector = Guice.createInjector(new MyModule(inMemoryBarCache));
        StrategyRunner runner = injector.getInstance(StrategyRunner.class);
        ValueAccumulatorCache vaCache = injector.getInstance(ValueAccumulatorCache.class);

        // We initialize by setting sma20 lower than sma50
        runner.initialize(script, "AAPL", new Bar(LocalDate.of(2024, 3, 14), 0, 0,0,90,0));
        SmaCalculator sma20 = (SmaCalculator) vaCache.getValueAccumulator(new SmaKey(20));
        SmaCalculator sma50 = (SmaCalculator) vaCache.getValueAccumulator(new SmaKey(50));
        CrossoverDetector cd1 = (CrossoverDetector) vaCache.getValueAccumulator(new CrossoverKey(new SmaKey(20), new SmaKey(50)));
        CrossoverDetector cd2 = (CrossoverDetector) vaCache.getValueAccumulator(new CrossoverKey(new SmaKey(50), new SmaKey(20)));
        assertEquals(99.5, sma20.getAverage());
        assertEquals(99.8, sma50.getAverage());
        assertFalse(cd1.getValue()); // Should be false, no crossover can happen on initialization
        assertFalse(cd2.getValue()); // Should be false, no crossover can happen on initialization


        runner.roll(new Bar(LocalDate.of(2024, 3, 15), 0, 0,0,160,0));
        assertEquals(102.5, sma20.getAverage());
        assertEquals(101, sma50.getAverage());
        assertTrue(cd1.getValue()); // 20 crosses over 50, should be true
        assertFalse(cd2.getValue());


        assertEquals(1, orderCache.snapshot().size());

        runner.roll(new Bar(LocalDate.of(2024, 3, 15), 0, 0,0,150,0));
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

        BarCache inMemoryBarCache = new InMemoryBarCache(businessDayService);

        injector = Guice.createInjector(new MyModule(inMemoryBarCache));

        LocalDate currentDate = LocalDate.of(2024, 1, 2);
        LocalDate endDate = LocalDate.of(2025, 5, 1);

        Map<LocalDate, Bar> initialValues = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            initialValues.put(currentDate, bars.get(currentDate));
            currentDate = businessDayService.nextBusinessDay(currentDate);
        }
        StrategyRunner runner = injector.getInstance(StrategyRunner.class);
        PositionManager positionManager = injector.getInstance(PositionManager.class);

        inMemoryBarCache.loadCache(initialValues);

        runner.initialize(script, "AAPL", bars.get(currentDate));
        currentDate = businessDayService.nextBusinessDay(currentDate);

        while (currentDate.isBefore(endDate) || currentDate.equals(endDate)) {
            // First we check if we need to make any trades
            runner.roll(bars.get(currentDate));
            currentDate = businessDayService.nextBusinessDay(currentDate);
        }
        assertEquals(8, orderCache.snapshot().size());
        List<Order> orders = new ArrayList<>(orderCache.snapshot().values());
        orders.sort(Comparator.comparing(Order::tradeDate));
        assertEquals(LocalDate.of(2024, 5, 9), orders.get(0).tradeDate());
        assertEquals(LocalDate.of(2024, 8, 21), orders.get(1).tradeDate());
        assertEquals(LocalDate.of(2024, 8, 30), orders.get(2).tradeDate());
        assertEquals(LocalDate.of(2024, 11, 20), orders.get(3).tradeDate());
        assertEquals(LocalDate.of(2024, 12, 4), orders.get(4).tradeDate());
        assertEquals(LocalDate.of(2025, 1, 28), orders.get(5).tradeDate());
        assertEquals(LocalDate.of(2025, 3, 7), orders.get(6).tradeDate());
        assertEquals(LocalDate.of(2025, 3, 18), orders.get(7).tradeDate());

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

        BarCache inMemoryBarCache = new InMemoryBarCache(businessDayService);


        injector = Guice.createInjector(new MyModule(inMemoryBarCache));

        LocalDate currentDate = LocalDate.of(2024, 1, 2);
        LocalDate endDate = LocalDate.of(2025, 5, 1);

        Map<LocalDate, Bar> initialValues = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            initialValues.put(currentDate, bars.get(currentDate));
            currentDate = businessDayService.nextBusinessDay(currentDate);
        }
        StrategyRunner runner = injector.getInstance(StrategyRunner.class);
        PositionManager positionManager = injector.getInstance(PositionManager.class);

        inMemoryBarCache.loadCache(initialValues);

        runner.initialize(script, "AAPL", bars.get(currentDate));
        currentDate = businessDayService.nextBusinessDay(currentDate);

        while (currentDate.isBefore(endDate) || currentDate.equals(endDate)) {
            // First we check if we need to make any trades
            runner.roll(bars.get(currentDate));
            currentDate = businessDayService.nextBusinessDay(currentDate);
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

        BarCache inMemoryBarCache = new InMemoryBarCache(businessDayService);

        injector = Guice.createInjector(new MyModule(inMemoryBarCache));

        LocalDate currentDate = LocalDate.of(2024, 1, 2);
        LocalDate endDate = LocalDate.of(2025, 5, 1);

        Map<LocalDate, Bar> initialValues = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            initialValues.put(currentDate, bars.get(currentDate));
            currentDate = businessDayService.nextBusinessDay(currentDate);
        }
        StrategyRunner runner = injector.getInstance(StrategyRunner.class);
        PositionManager positionManager = injector.getInstance(PositionManager.class);

        inMemoryBarCache.loadCache(initialValues);

        runner.initialize(script, "AAPL", bars.get(currentDate));
        currentDate = businessDayService.nextBusinessDay(currentDate);

        while (currentDate.isBefore(endDate) || currentDate.equals(endDate)) {
            // First we check if we need to make any trades
            runner.roll(bars.get(currentDate));
            currentDate = businessDayService.nextBusinessDay(currentDate);
        }
        assertEquals(12, orderCache.snapshot().size());
        List<Order> orders = new ArrayList<>(orderCache.snapshot().values());
        orders.sort(Comparator.comparing(Order::tradeDate));
        assertEquals(LocalDate.of(2024, 5, 9), orders.get(0).tradeDate());
        assertEquals(LocalDate.of(2024, 5, 9), orders.get(1).tradeDate());
        assertEquals(LocalDate.of(2024, 8, 21), orders.get(2).tradeDate());
        assertEquals(LocalDate.of(2024, 8, 30), orders.get(3).tradeDate());
        assertEquals(LocalDate.of(2024, 8, 30), orders.get(4).tradeDate());
        assertEquals(LocalDate.of(2024, 11, 20), orders.get(5).tradeDate());
        assertEquals(LocalDate.of(2024, 12, 4), orders.get(6).tradeDate());
        assertEquals(LocalDate.of(2024, 12, 4), orders.get(7).tradeDate());
        assertEquals(LocalDate.of(2025, 1, 28), orders.get(8).tradeDate());
        assertEquals(LocalDate.of(2025, 3, 7), orders.get(9).tradeDate());
        assertEquals(LocalDate.of(2025, 3, 7), orders.get(10).tradeDate());
        assertEquals(LocalDate.of(2025, 3, 18), orders.get(11).tradeDate());

        assertEquals(0, positionManager.openTrades());
        assertEquals(8, positionManager.closedTrades());
        assertEquals(43_960.00, positionManager.grossProfit());
        assertEquals(35_100.00, positionManager.grossLoss());
        assertEquals(8_860.00, positionManager.netProfit());
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
            bind(BusinessDayService.class).toInstance(businessDayService);
            bind(OrderCache.class).toInstance(orderCache);
            bind(ValueAccumulatorCache.class).toInstance(vaCache);
            bind(BarCache.class).toInstance(inMemoryBarCache);
        }
    }
}
