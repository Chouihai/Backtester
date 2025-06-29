package HaitamStockProject.objects.valueaccumulator;

import HaitamStockProject.objects.Bar;

public class CrossoverDetector implements ValueAccumulator<Boolean> {

    private final ValueAccumulator<Double> va1;
    private final ValueAccumulator<Double> va2;
    private int previousSign = 0;

    public CrossoverDetector(ValueAccumulator<Double> va1, ValueAccumulator<Double> va2) {
        this.va1 = va1;
        this.va2 = va2;
        double diff = va1.getValue() - va2.getValue();
        if (diff > 0) {
            previousSign = 1;
        } else if (diff < 0) {
            previousSign = -1;
        }
    }

    // Need to be created with separate instances of ValueAccumulators
    public Boolean getValue() {
        double diff = va1.getValue() - va2.getValue();
        int curr_sign = 0;
        if (diff > 0) {
            curr_sign = 1;
        } else if (diff < 0) {
            curr_sign = -1;
        }
        return previousSign != 0 && curr_sign > 0 && curr_sign != previousSign;
    }

    public void roll(Bar bar) {
        double diff = va1.getValue() - va1.getValue();
        if (diff > 0) {
            previousSign = 1;
        } else if (diff < 0) {
            previousSign = -1;
        }
        va1.roll(bar);
        va2.roll(bar);
    }

    public CrossoverDetector copy() {
        return new CrossoverDetector(va1.copy(), va2.copy());
    }
}
