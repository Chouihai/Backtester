package HaitamStockProject.strategies;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmaCalculatorTest {

    @Test
    void testInitialAverageWithExactDays() {
        List<Double> values = Arrays.asList(1.0, 2.0, 3.0);
        SmaCalculator calc = new SmaCalculator(3, values);
        assertEquals(2.0, calc.getAverage(), 0.0001);
    }

    @Test
    void testInitialAverageWithMoreThanDays() {
        List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        SmaCalculator calc = new SmaCalculator(3, values);
        assertEquals(4.0, calc.getAverage(), 0.0001);
    }

    @Test
    void testInitialAverageWithFewerThanDays() {
        List<Double> values = Arrays.asList(2.0, 4.0);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new SmaCalculator(3, values);
        });
    }

    @Test
    void testAddValueUpdatesAverage() {
        List<Double> values = Arrays.asList(10.0, 20.0, 30.0);
        SmaCalculator calc = new SmaCalculator(3, values);
        calc.addValue(40.0); // should now be (20 + 30 + 40) / 3 = 30.0
        assertEquals(30.0, calc.getAverage(), 0.0001);
    }

    @Test
    void testAddValueMultipleTimes() {
        SmaCalculator calc = new SmaCalculator(3, Arrays.asList(1.0, 2.0, 3.0)); // avg = 2.0
        assertEquals(2.0, calc.getAverage(), 0.0001);

        calc.addValue(4.0);
        assertEquals(3.0, calc.getAverage(), 0.0001);

        calc.addValue(5.0);
        assertEquals(4.0, calc.getAverage(), 0.0001);

        calc.addValue(6.0);
        assertEquals(5.0, calc.getAverage(), 0.0001);
    }

    @Test
    void testAddValueWithWindowSizeOne() {
        List<Double> values = Collections.singletonList(100.0);
        SmaCalculator calc = new SmaCalculator(1, values);
        assertEquals(100.0, calc.getAverage(), 0.0001);

        calc.addValue(200.0);
        assertEquals(200.0, calc.getAverage(), 0.0001);
    }

    @Test
    void testEmptyInitialValues() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new SmaCalculator(3, Collections.emptyList());
        });
    }

    @Test
    void testNonPositiveNumDays() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new SmaCalculator(-3, Arrays.asList(1.0, 2.0, 3.0));
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new SmaCalculator(0, Arrays.asList(1.0, 2.0, 3.0));
        });
    }

    @Test
    void testNoNegativeValues() {
        List<Double> values = Arrays.asList(0.0, -1.0, 2.0);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new SmaCalculator(3, values);
        });
    }
}
