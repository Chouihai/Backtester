package HaitamStockProject.backtester.caches;

import HaitamStockProject.objects.Bar;
import HaitamStockProject.strategies.ValueAccumulator;

import java.util.List;

public interface BacktestRunValueAccumulatorCache {

    void intialize(List<Bar> initialValues);

    void put(String id, ValueAccumulator accumulator);

    ValueAccumulator getValueAccumulator(String id);

    void roll(Bar bar);
}
