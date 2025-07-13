package Backtester.script.functions.ohlcv;


import Backtester.caches.BarCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.valueaccumulator.OhlcvField;

public class HighFunction extends OhlcvFunction {

    public static final String FUNCTION_NAME = "high";

    public HighFunction(ValueAccumulatorCache cache, BarCache barCache) {
        super(cache, barCache);
    }

    public OhlcvField getField() {
        return OhlcvField.HIGH;
    }

    public String getFunctionName() {
        return FUNCTION_NAME;
    }
}