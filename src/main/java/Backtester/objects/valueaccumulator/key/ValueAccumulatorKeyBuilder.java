package Backtester.objects.valueaccumulator.key;

import Backtester.objects.valueaccumulator.OhlcvValueAccumulator;
import Backtester.objects.valueaccumulator.SmaCalculator;
import Backtester.objects.valueaccumulator.ValueAccumulator;

public class ValueAccumulatorKeyBuilder {

    public ValueAccumulatorKey build(ValueAccumulator<?> va) {
        return switch(va) {
            case SmaCalculator sma -> new SmaKey(sma.numDays);
            case OhlcvValueAccumulator ohlcv -> new OhlcvKey(ohlcv.getField(), ohlcv.getLookback());
            case null, default -> throw new RuntimeException("Could not find key for value accumulator!");
        };
    }
}
