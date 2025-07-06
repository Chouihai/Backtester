package Backtester.objects.valueaccumulator;

import Backtester.caches.BarCache;
import Backtester.objects.Bar;
import com.google.inject.Inject;

import java.util.List;

public class ValueAccumulatorFactory {

    private final BarCache barCache;

    @Inject
    public ValueAccumulatorFactory(BarCache barCache) {
        this.barCache = barCache;
    }

    public ValueAccumulator<?> create(ValueAccumulatorType type, int length, int currentIndex) {
        if (type == ValueAccumulatorType.SMA) {
            List<Bar> initialValues = barCache.getLastNDays(length, currentIndex);
            return new SmaCalculator(length, initialValues);
        }
        // Crossover detector isn't here because it's easy to create.
        return null;
    }
}
