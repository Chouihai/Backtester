package Backtester.objects.valueaccumulator;

import Backtester.caches.BarCache;
import Backtester.objects.Bar;
import Backtester.objects.valueaccumulator.key.ValueAccumulatorKey;
import com.google.inject.Inject;

import java.util.List;

public class ValueAccumulatorFactory {

    private final BarCache barCache;

    @Inject
    public ValueAccumulatorFactory(BarCache barCache) {
        this.barCache = barCache;
    }

    public ValueAccumulator<?> createValueAccumulator(ValueAccumulatorType type, ValueAccumulatorKey key) {
        return null;
    }

    public ValueAccumulator<?> create(ValueAccumulatorType type, int length, Bar startingBar) {
        if (type == ValueAccumulatorType.SMA) {
            List<Bar> initialValues = barCache.getLastNDays(length, startingBar);
            initialValues.add(startingBar);
            return new SmaCalculator(length, initialValues);
        }
        return null;
    }
}
