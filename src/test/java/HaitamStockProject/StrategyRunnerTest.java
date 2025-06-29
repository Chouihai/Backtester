package HaitamStockProject;

import HaitamStockProject.backtester.caches.*;
import HaitamStockProject.caches.InMemoryOrderCache;
import HaitamStockProject.caches.MockPositionCache;
import HaitamStockProject.objects.Bar;
import HaitamStockProject.objects.SecurityDayValues;
import HaitamStockProject.objects.valueaccumulator.CrossoverDetector;
import HaitamStockProject.objects.valueaccumulator.ValueAccumulatorFactory;
import HaitamStockProject.objects.valueaccumulator.key.CrossoverKey;
import HaitamStockProject.objects.valueaccumulator.key.SmaKey;
import HaitamStockProject.services.BusinessDayService;
import HaitamStockProject.services.MockBusinessDayService;
import HaitamStockProject.objects.valueaccumulator.SmaCalculator;
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
    private final PositionCache positionCache = new MockPositionCache();
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

            SecurityDayValues value = new SecurityDayValues(
                    1,
                    date,
                    open,
                    0.0, // high
                    0.0, // low
                    close,
                    0L,  // volume
                    0.0, // vwap
                    0    // numTrades
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

        // We initial by setting sma20 lower than sma50
        runner.initialize(script, "AAPL", new Bar(LocalDate.of(2024, 3, 14), 0, 0,0,90,0));
        SmaCalculator va = (SmaCalculator) vaCache.getValueAccumulator(new SmaKey(20));
        SmaCalculator va2 = (SmaCalculator) vaCache.getValueAccumulator(new SmaKey(50));
        CrossoverDetector cd1 = (CrossoverDetector) vaCache.getValueAccumulator(new CrossoverKey(new SmaKey(20), new SmaKey(50)));
        CrossoverDetector cd2 = (CrossoverDetector) vaCache.getValueAccumulator(new CrossoverKey(new SmaKey(50), new SmaKey(20)));
        assertEquals(99.5, va.getAverage());
        assertEquals(99.8, va2.getAverage());
        assertFalse(cd1.getValue()); // Should be false, no crossover can happen on initialization
        assertFalse(cd2.getValue()); // Should be false, no crossover can happen on initialization


        runner.roll(new Bar(LocalDate.of(2024, 3, 15), 0, 0,0,160,0));
        assertEquals(102.5, va.getAverage());
        assertEquals(101, va2.getAverage());
        assertTrue(cd1.getValue()); // 20 crosses over 50, should be true
        assertFalse(cd2.getValue());


        assertEquals(1, orderCache.snapshot().size());
        orderCache.snapshot().values();
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
            bind(PositionCache.class).toInstance(positionCache);
            bind(OrderCache.class).toInstance(orderCache);
            bind(ValueAccumulatorCache.class).toInstance(vaCache);
            bind(BarCache.class).toInstance(inMemoryBarCache);
        }
    }
}
