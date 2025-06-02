package HaitamStockProject.strategies;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Constant time SMA calculator
 */
public class SmaCalculator {

    private final int numDays;
    private final Queue<Double> values = new LinkedList<>();
    private double sumOfAllValues = 0.0;

    public SmaCalculator(int numDays, List<Double> initialValues) {
        if (numDays <= 0 || initialValues.size() < numDays) throw new IllegalArgumentException();
        this.numDays = numDays;
        for (Double d : initialValues) {
            this.addValue(d);
        }
    }

    public void addValue(double latest) {
        if (latest < 0) throw new IllegalArgumentException();
        Double oldest = 0.0;
        if (values.size() == numDays) {
            oldest = values.poll();
        }
        values.add(latest);
        this.sumOfAllValues = sumOfAllValues - oldest + latest;
    }

    public double getAverage() {
        return sumOfAllValues / numDays;
    }
}
