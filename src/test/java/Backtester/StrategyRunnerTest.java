package Backtester;


import Backtester.objects.Bar;
import Backtester.objects.Trade;
import Backtester.strategies.RunResult;
import Backtester.strategies.StrategyRunner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StrategyRunnerTest {

    private static final List<Bar> bars = new ArrayList<>();
    private static final List<Bar> initialBars = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(StrategyRunnerTest.class);

    @BeforeAll
    static void setupOnce() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = StrategyRunnerTest.class.getResourceAsStream("/AAPL.JSON")) {
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
                if (i <= 50) {
                    initialBars.add(value);
                } else bars.add(value);
            }
        }
    }

    @Test
    void test_unchanged() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)

                if sma20 > sma50:
                    createOrder("long", true, 10)
                if sma50 > sma20:
                    createOrder("position1", false, 10)
                """;

        LocalDate currentDate = LocalDate.of(2024, 1, 2);
        List<Bar> initialValues = new ArrayList<>();
        // 50 days of flat prices, no crossover at start
        for (int i = 0; i < 50; i++) {
            initialValues.add(new Bar(i, currentDate, 0, 0,0,100,0));
            currentDate = currentDate.plusDays(1);
        }
        initialValues.add(new Bar(50, currentDate.plusDays(1), 0, 0,0,100,0));

        StrategyRunner runner = new StrategyRunner(initialValues, initialValues, script, logger);
        RunResult result = runner.run(100_000);

        // No trades expected; net PnL should be zero
        assertTrue(result.trades().isEmpty());
        assertEquals(0.0, result.netProfit());
    }

    @Test
    void test_bullish() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)

                if crossover(sma20, sma50):
                    createOrder("long", true, 10)
                if crossover(sma50, sma20):
                    createOrder("position1", false, 10)
                """;

        LocalDate currentDate = LocalDate.of(2024, 1, 2);
        List<Bar> series = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            series.add(new Bar(i, currentDate, 0, 0,0,100,0));
            currentDate = currentDate.plusDays(1);
        }
        List<Bar> newBars = new ArrayList<>();
        newBars.add(new Bar(50, currentDate.plusDays(1), 0, 0,0,90,0));
        newBars.add(new Bar(51, currentDate.plusDays(2), 0, 0,0,160,0));
        newBars.add(new Bar(52, currentDate.plusDays(2), 0, 0,0,160,0));

        StrategyRunner runner = new StrategyRunner(newBars, series, script, logger);
        RunResult result = runner.run(100_000);
        // At least one trade should be opened on crossover
        assertTrue(result.trades().size() >= 1);
    }

    @Test
    void irlSimulation() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)

                if crossover(sma20, sma50):
                    createOrder("long", true, 1000)
                if crossover(sma50, sma20):
                    createOrder("position1", false, 1000)
                """;

        StrategyRunner runner = new StrategyRunner(bars, initialBars, script, logger);
        RunResult result = runner.run(100_000);

        // Expect fully closed by end according to previous expectations
        long open = result.trades().stream().filter(Trade::isOpen).count();
        long closed = result.trades().stream().filter(Trade::isClosed).count();

        assertEquals(0, open);
        assertEquals(4, closed);
        assertEquals(43_960.00, result.grossProfit());
        assertEquals(35_100.00, result.grossLoss());
        assertEquals(8_860.00, result.netProfit());
    }

    @Test
    void irlSimulation2() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)

                if crossover(sma20, sma50):
                    createOrder("long", true, 1000)
                if crossover(sma50, sma20):
                    createOrder("position1", false, 500)
                """;

        StrategyRunner runner = new StrategyRunner(bars, initialBars, script, logger);
        RunResult result = runner.run(100_000);

        long open = result.trades().stream().filter(Trade::isOpen).count();
        long closed = result.trades().stream().filter(Trade::isClosed).count();

        assertEquals(2, open);
        assertEquals(4, closed);
        assertEquals(45_060.00, result.grossProfit());
        assertEquals(8_015.00, result.grossLoss());
        assertEquals(37_045.00, result.netProfit());
        assertEquals(-59_820.00, result.openPnL());
    }

    @Test
    void irlSimulation3() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)

                if crossover(sma20, sma50):
                    createOrder("long", true, 500)
                    createOrder("long2", true, 500)
                if crossover(sma50, sma20):
                    createOrder("position1", false, 1000)
                """;

        StrategyRunner runner = new StrategyRunner(bars, initialBars, script, logger);
        RunResult result = runner.run(100_000);

        long open = result.trades().stream().filter(Trade::isOpen).count();
        long closed = result.trades().stream().filter(Trade::isClosed).count();

        assertEquals(0, open);
        assertEquals(8, closed);
        assertEquals(43_960.00, result.grossProfit());
        assertEquals(35_100.00, result.grossLoss());
        assertEquals(8_860.00, result.netProfit());
    }

    @Test
    void irlSimulation4() {
        String script = """
                sma20 = sma(20)
                sma50 = sma(50)

                if crossover(sma20, sma50):
                    createOrder("A", true, 600)
                    createOrder("B", true, 500)
                if crossover(sma50, sma20):
                    createOrder("position1", false, 500)
                """;

        StrategyRunner runner = new StrategyRunner(bars, initialBars, script, logger);
        RunResult result = runner.run(100_000);

        long open = result.trades().stream().filter(Trade::isOpen).count();
        long closed = result.trades().stream().filter(Trade::isClosed).count();

        assertEquals(5, open);
        assertEquals(7, closed);
        assertEquals(49_823.00, result.grossProfit());
        assertEquals(8_015.00, result.grossLoss());
        assertEquals(41_808.00, result.netProfit());
    }
}
