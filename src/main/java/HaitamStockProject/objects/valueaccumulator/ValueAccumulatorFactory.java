package HaitamStockProject.objects.valueaccumulator;

import HaitamStockProject.backtester.caches.BarCache;
import HaitamStockProject.objects.Bar;
import com.google.inject.Inject;

import java.util.List;
import java.util.Objects;

public class ValueAccumulatorFactory {

    private final BarCache barCache;

    @Inject()
    public ValueAccumulatorFactory(BarCache barCache) {
        this.barCache = barCache;
    }

    public ValueAccumulator<?> create(ValueAccumulatorType type, int length, Bar startingBar) {
        if (Objects.requireNonNull(type) == ValueAccumulatorType.SMA) {
            List<Bar> initialValues = barCache.getLastNDays(length, startingBar);
            initialValues.add(startingBar);
            return new SmaCalculator(length, initialValues);
        }
        return null;
    }
}
