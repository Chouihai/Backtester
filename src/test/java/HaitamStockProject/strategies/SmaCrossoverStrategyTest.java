//package HaitamStockProject.strategies;
//
//import HaitamStockProject.objects.order.Order;
//import HaitamStockProject.objects.SecurityDayValues;
//import HaitamStockProject.services.BusinessDayService;
//import HaitamStockProject.services.MockBusinessDayService;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class SmaCrossoverStrategyTest {
//
//    private static BusinessDayService businessDayService;
//    private SmaCrossoverStrategy strategy;
//    private final LocalDate startDate = LocalDate.of(2024, 1, 1);
//    private final String aaplSymbol = "AAPL";
//    private final SecurityDayValues emptyValues = new SecurityDayValues(1, startDate, 0, 0, 0,0,0,0,0);
//    private final static Map<LocalDate, SecurityDayValues> timeSeries = new HashMap<>();
//
//    private static final Set<LocalDate> holidays = new HashSet<>();
//
//    @BeforeAll
//    static void setupOnce() throws IOException {
//        // ---- 2024 Holidays ----
//        holidays.add(LocalDate.of(2024, 1, 1));  // New Year's Day
//        holidays.add(LocalDate.of(2024, 1, 15)); // MLK Jr. Day
//        holidays.add(LocalDate.of(2024, 2, 19)); // Presidents’ Day
//        holidays.add(LocalDate.of(2024, 3, 29)); // Good Friday
//        holidays.add(LocalDate.of(2024, 5, 27)); // Memorial Day
//        holidays.add(LocalDate.of(2024, 6, 19)); // Juneteenth
//        holidays.add(LocalDate.of(2024, 7, 4));  // Independence Day
//        holidays.add(LocalDate.of(2024, 9, 2));  // Labor Day
//        holidays.add(LocalDate.of(2024, 11, 28)); // Thanksgiving
//        holidays.add(LocalDate.of(2024, 12, 25)); // Christmas
//
//        // ---- 2025 Holidays ----
//        holidays.add(LocalDate.of(2025, 1, 1));  // New Year's Day
//        holidays.add(LocalDate.of(2025, 1, 9));  // Jimmy Carter died, RIP da goat
//        holidays.add(LocalDate.of(2025, 1, 20)); // MLK Jr. Day
//        holidays.add(LocalDate.of(2025, 2, 17)); // Presidents’ Day
//        holidays.add(LocalDate.of(2025, 4, 18)); // Good Friday
//        businessDayService = new MockBusinessDayService(holidays);
//
//        ObjectMapper mapper = new ObjectMapper();
//        InputStream is = SmaCrossoverStrategyTest.class.getResourceAsStream("/AAPL.JSON");
//        JsonNode root = mapper.readTree(is);
//        JsonNode results = root.get("results");
//
//        int securityId = 1;
//
//        for (JsonNode node : results) {
//            long timestampMillis = node.get("t").asLong();
//            double open = node.get("o").asDouble();
//            double close = node.get("c").asDouble();
//
//            LocalDate date = Instant.ofEpochMilli(timestampMillis)
//                    .atZone(ZoneId.of("GMT"))
//                    .toLocalDate();
//
//            SecurityDayValues value = new SecurityDayValues(
//                    securityId,
//                    date,
//                    open,
//                    0.0, // high
//                    0.0, // low
//                    close,
//                    0L,  // volume
//                    0.0, // vwap
//                    0    // numTrades
//            );
//
//            timeSeries.put(date, value);
//        }
//
//    }
//
//    @Test
//    void averagesEqualThroughout() {
//        List<Double> initialValues = new ArrayList<>();
//        // 50 days of flat prices, no crossover at start
//        for (int i = 0; i < 50; i++) {
//            initialValues.add(100.0);
//        }
//
//        strategy = new SmaCrossoverStrategy(
//                20,
//                50,
//                10,
//                aaplSymbol,
//                100.0,
//                startDate,
//                initialValues,
//                businessDayService
//        );
//        LocalDate date = startDate;
//        for (int i = 0; i < 10; i++) {
//            date = businessDayService.nextBusinessDay(date);
//            SecurityDayValues day = emptyValues.withDate(date).withClosePrice(100.0).withOpenPrice(100.0);
//            assertTrue(strategy.roll(day).isEmpty());
//        }
//    }
//
//    @Test
//    void bearishCrossover() {
//        // Push 20-day SMA above 50-day by increasing close prices
//        List<Double> initialValues = new ArrayList<>();
//        // 50 days of flat prices, no crossover at start
//        for (int i = 0; i < 49; i++) {
//            initialValues.add(100.0);
//        }
//        initialValues.add(110.0); // Should bring sma20 up
//        strategy = new SmaCrossoverStrategy(
//                20,
//                50,
//                10,
//                aaplSymbol,
//                100.0,
//                startDate,
//                initialValues,
//                businessDayService
//        );
//
//        LocalDate date = startDate;
//        double close = 50.0; // Should bring sma50 back up
//        double open = 111.0;
//        date = businessDayService.nextBusinessDay(date);
//        SecurityDayValues day = emptyValues.withDate(date).withClosePrice(close).withOpenPrice(open);
//
//        List<Order> trades = strategy.roll(day);
//        assertEquals(1, trades.size());
//        Order order = trades.get(0);
//        assertEquals("AAPL", order.symbol());
//        assertEquals(-10, order.signedQuantity());
//        assertEquals(111.0, order.price(), 0.0001);
//
//        // roll again to make sure it only makes orders at a cross
//        date = businessDayService.nextBusinessDay(date);
//        day = day.withDate(date);
//        trades = strategy.roll(day);
//        assertEquals(0, trades.size());
//    }
//
//    @Test
//    void bullishCrossover() {
//        List<Double> initialValues = new ArrayList<>();
//        for (int i = 0; i < 49; i++) {
//            initialValues.add(100.0);
//        }
//        initialValues.add(90.0); // Should make sma50 higher
//        strategy = new SmaCrossoverStrategy(
//                20,
//                50,
//                10,
//                aaplSymbol,
//                100.0,
//                startDate,
//                initialValues,
//                businessDayService
//        );
//
//        LocalDate date = startDate;
//        double close = 150.0; // Should bring sma20 back up
//        double open = 111.0;
//        date = businessDayService.nextBusinessDay(date);
//        SecurityDayValues day = emptyValues.withDate(date).withClosePrice(close).withOpenPrice(open);
//
//        List<Order> trades = strategy.roll(day);
//        assertEquals(1, trades.size());
//        Order order = trades.get(0);
//        assertEquals("AAPL", order.symbol());
//        assertEquals(10, order.signedQuantity());
//        assertEquals(111.0, order.price(), 0.0001);
//
//        // roll again to make sure it only makes orders at a cross
//        date = businessDayService.nextBusinessDay(date);
//        day = day.withDate(date);
//        trades = strategy.roll(day);
//        assertEquals(0, trades.size());
//    }
//
//    @Test
//    void bearishThenEqualThenBullish() {
//        List<Double> initialValues = new ArrayList<>();
//        for (int i = 0; i < 49; i++) {
//            initialValues.add(100.0);
//        }
//        initialValues.add(90.0); // Should make sma50 higher
//        strategy = new SmaCrossoverStrategy(
//                20,
//                50,
//                10,
//                aaplSymbol,
//                100.0,
//                startDate,
//                initialValues,
//                businessDayService
//        );
//
//        LocalDate date = startDate;
//        double close = 110.0; // should make them equal
//        double open = 111.0;
//        date = businessDayService.nextBusinessDay(date);
//        SecurityDayValues day = emptyValues.withDate(date).withClosePrice(close).withOpenPrice(open);
//
//        List<Order> trades = strategy.roll(day);
//        assertEquals(0, trades.size()); // they are equal and thus no crossover happened
//
//
//        //  make sma20 higher now and roll again, and should detect a cross
//        date = businessDayService.nextBusinessDay(date);
//        day = day.withDate(date).withClosePrice(150.0);
//        trades = strategy.roll(day);
//        assertEquals(1, trades.size());
//        Order order = trades.get(0);
//        assertEquals("AAPL", order.symbol());
//        assertEquals(10, order.signedQuantity());
//        assertEquals(111.0, order.price(), 0.0001);
//    }
//
//    @Test
//    void bearishThenEqualThenBearish() {
//        List<Double> initialValues = new ArrayList<>();
//        for (int i = 0; i < 49; i++) {
//            initialValues.add(100.0);
//        }
//        initialValues.add(90.0); // Should make sma50 higher
//        strategy = new SmaCrossoverStrategy(
//                20,
//                50,
//                10,
//                aaplSymbol,
//                100.0,
//                startDate,
//                initialValues,
//                businessDayService
//        );
//
//        LocalDate date = startDate;
//        double close = 110.0; // should make them equal
//        double open = 111.0;
//        date = businessDayService.nextBusinessDay(date);
//        SecurityDayValues day = emptyValues.withDate(date).withClosePrice(close).withOpenPrice(open);
//
//        List<Order> trades = strategy.roll(day);
//        assertEquals(0, trades.size()); // they are equal and thus no crossover happened
//
//
//        //  make sma20 lower again and no orders should be made as a cross never actually happened (they intersected but did not cross)
//        date = businessDayService.nextBusinessDay(date);
//        day = day.withDate(date).withClosePrice(90.0);
//        trades = strategy.roll(day);
//        assertEquals(0, trades.size());
//    }
//
//    @Test
//    void divergenceFromInitialEquality() {
//        List<Double> initialValues = new ArrayList<>();
//        for (int i = 0; i < 50; i++) {
//            initialValues.add(100.0);
//        }
//        strategy = new SmaCrossoverStrategy(
//                20,
//                50,
//                10,
//                aaplSymbol,
//                100.0,
//                startDate,
//                initialValues,
//                businessDayService
//        );
//
//        LocalDate date = startDate;
//        double close = 110.0; // should make sma20 higher
//        double open = 111.0;
//        date = businessDayService.nextBusinessDay(date);
//        SecurityDayValues day = emptyValues.withDate(date).withClosePrice(close).withOpenPrice(open);
//
//        List<Order> trades = strategy.roll(day);
//        assertEquals(0, trades.size()); // they diverged from equality, no cross ever happened
//        // This could change in the future, but it's an incredibly rare edge case to have both averages be equal at the start to the decimal.
//    }
//
//    @Test
//    void irlSimulation() {
//        assertEquals(timeSeries.values().size(), 334);
//        for (LocalDate holiday: holidays) {
//            assertNull(timeSeries.get(holiday));
//        }
//        LocalDate currentDate = LocalDate.of(2024, 1, 2);
//        LocalDate endDate = LocalDate.of(2025, 5, 1);
//        List<Order> allOrders = new ArrayList<>();
//
//        List<Double> initialValues = new ArrayList<>();
//        for (int i = 0; i < 50; i++) {
//            initialValues.add(timeSeries.get(currentDate).getClose());
//            currentDate = businessDayService.nextBusinessDay(currentDate);
//        }
//        strategy = new SmaCrossoverStrategy(
//                20,
//                50,
//                10,
//                aaplSymbol,
//                100.0,
//                currentDate,
//                initialValues,
//                businessDayService
//        );
//
//        while (currentDate.isBefore(endDate)) {
//            // First we check if we need to make any trades
//            List<Order> orders = strategy.roll(timeSeries.get(currentDate));
//            if (!orders.isEmpty()) {
//                allOrders.addAll(orders);
//            }
//            currentDate = businessDayService.nextBusinessDay(currentDate);
//        }
//        assertEquals(8, allOrders.size());
//    }
//}
