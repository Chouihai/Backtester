package Backtester.strategies;

import Backtester.caches.InMemoryOrderCache;
import Backtester.caches.OrderCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.Bar;
import Backtester.trades.PositionManager;

import java.util.List;

public class RunContext {

    public PositionManager positionManager;
    public OrderCache orderCache;
    public List<Bar> bars;
    public ValueAccumulatorCache valueAccumulatorCache;
    public int currentIndex;

    public RunContext(List<Bar> bars) {
        orderCache = new InMemoryOrderCache();
        positionManager = new PositionManager(orderCache);
        valueAccumulatorCache = new ValueAccumulatorCache();
        this.bars = bars;
        this.currentIndex = 0;
    }

    public void roll(Bar bar) {
        currentIndex = bar.index;
        positionManager.roll(bar);
        valueAccumulatorCache.roll(bar);
    }
}
