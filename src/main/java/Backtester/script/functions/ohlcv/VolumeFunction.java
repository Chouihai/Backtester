package Backtester.script.functions.ohlcv;

import Backtester.caches.BarCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.valueaccumulator.OhlcvField;

public class VolumeFunction extends OhlcvFunction {

    public static final String FUNCTION_NAME = "volume";

    public VolumeFunction(ValueAccumulatorCache cache, BarCache barCache) {
        super(cache, barCache);
    }

    public OhlcvField getField() {
        return OhlcvField.VOLUME;
    }

    public String getFunctionName() {
        return FUNCTION_NAME;
    }
}