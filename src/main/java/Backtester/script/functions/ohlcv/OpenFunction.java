package Backtester.script.functions.ohlcv;

import Backtester.caches.BarCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.valueaccumulator.OhlcvField;

public class OpenFunction extends OhlcvFunction {

    public static final String FUNCTION_NAME = "open";

    public OpenFunction(ValueAccumulatorCache cache, BarCache barCache) {
        super(cache, barCache);
    }

    public OhlcvField getField() {
        return OhlcvField.OPEN;
    }

    public String getFunctionName() {
        return FUNCTION_NAME;
    }
}