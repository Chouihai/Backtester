package Backtester.script.functions.ohlcv;

import Backtester.caches.BarCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.valueaccumulator.OhlcvField;

public class CloseFunction extends OhlcvFunction {

    public static final String FUNCTION_NAME = "close";

    public CloseFunction(ValueAccumulatorCache cache, BarCache barCache) {
        super(cache, barCache);
    }

    public OhlcvField getField() {
        return OhlcvField.CLOSE;
    }

    public String getFunctionName() {
        return FUNCTION_NAME;
    }
}