package HaitamStockProject.backtester.caches;

import HaitamStockProject.objects.Bar;
import HaitamStockProject.objects.valueaccumulator.ValueAccumulator;
import HaitamStockProject.objects.valueaccumulator.key.ValueAccumulatorKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * In memory cache, does not read anything from database
 */
@Singleton
public class ValueAccumulatorCache {

    private final Map<ValueAccumulatorKey, ValueAccumulator<?>> valueAccumulators = new HashMap<>();

    public boolean contains(ValueAccumulatorKey key) {
        return valueAccumulators.containsKey(key);
    }

    public void put(ValueAccumulatorKey valueAccumulatorKey, ValueAccumulator<?> valueAccumulator) {
        valueAccumulators.put(valueAccumulatorKey, valueAccumulator);
    }

    public ValueAccumulator<?> getValueAccumulator(ValueAccumulatorKey key) {
        return valueAccumulators.get(key);
    }

    public void roll(Bar bar) {
        valueAccumulators.values().forEach(va -> va.roll(bar));
    }
}

