package HaitamStockProject.objects.valueaccumulator;

import HaitamStockProject.objects.Bar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Constant time SMA calculator
 */
public class SmaCalculator implements ValueAccumulator<Double> {

    public final int numDays;
    private final Queue<Double> values = new LinkedList<>();
    private double sumOfAllValues = 0.0;
    private final List<Bar> initialValues;

    public SmaCalculator(int numDays, List<Bar> initialValues) {
        if (numDays <= 0 || initialValues.size() < numDays) throw new IllegalArgumentException();
        this.initialValues = new ArrayList<>(initialValues);
        this.numDays = numDays;
        for (Bar bar : initialValues) {
            this.addValue(bar.close);
        }
    }

    public void roll(Bar bar) {
        addValue(bar.close);
    }

    // Probably make this private later
    public void addValue(Double latest) {
        if (latest < 0) throw new IllegalArgumentException();
        Double oldest = 0.0;
        if (values.size() == numDays) {
            oldest = values.poll();
        }
        values.add(latest);
        this.sumOfAllValues = sumOfAllValues - oldest + latest;
    }

    public Double getValue() {
        return getAverage();
    }

    public double getAverage() {
        return sumOfAllValues / numDays;
    }

    public SmaCalculator copy() {
        return new SmaCalculator(numDays, initialValues);
    }
}
