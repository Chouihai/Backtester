package Backtester.strategies;

import Backtester.caches.InMemoryOrderCache;
import Backtester.caches.OrderCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.Bar;
import Backtester.trades.PositionManager;

import java.util.List;

public class RunContext {

    final public PositionManager positionManager;
    final public OrderCache orderCache;
    final public List<Bar> bars;
    final public List<Bar> lookbackBars;
    final public ValueAccumulatorCache valueAccumulatorCache;
    public int currentIndex;

    public RunContext(List<Bar> bars, List<Bar> lookbackBars) {
        orderCache = new InMemoryOrderCache();
        positionManager = new PositionManager(orderCache);
        valueAccumulatorCache = new ValueAccumulatorCache();
        this.bars = bars;
        this.currentIndex = 0;
        this.lookbackBars = lookbackBars;
    }

    public void roll(Bar bar) {
        currentIndex++;
        valueAccumulatorCache.roll(bar);
        positionManager.roll(bar);
    }
}
