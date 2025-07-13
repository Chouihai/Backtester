package Backtester.script.functions.ohlcv;

import Backtester.caches.BarCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.valueaccumulator.OhlcvField;

public class LowFunction extends OhlcvFunction {

    public static final String FUNCTION_NAME = "low";

    public LowFunction(ValueAccumulatorCache cache, BarCache barCache) {
        super(cache, barCache);
    }

    public OhlcvField getField() {
        return OhlcvField.LOW;
    }

    public String getFunctionName() {
        return FUNCTION_NAME;
    }
}