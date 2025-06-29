package HaitamStockProject.objects.valueaccumulator.key;

import HaitamStockProject.objects.valueaccumulator.SmaCalculator;
import HaitamStockProject.objects.valueaccumulator.ValueAccumulator;

public class ValueAccumulatorKeyBuilder {

    public ValueAccumulatorKey build(ValueAccumulator<?> va) {
        return switch(va) {
            case SmaCalculator sma -> new SmaKey(sma.numDays);
            case null, default -> throw new RuntimeException("Could not find key for value accumulator!");
        };
    }
}
