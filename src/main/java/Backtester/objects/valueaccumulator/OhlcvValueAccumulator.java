package Backtester.objects.valueaccumulator;

import Backtester.objects.Bar;

import java.util.List;

public class OhlcvValueAccumulator implements ValueAccumulator<Double> {

    private final OhlcvField field;
    private final int lookback;
    private final List<Bar> bars;
    private int currentIndex;

    public OhlcvValueAccumulator(OhlcvField field, int lookback, List<Bar> bars, int currentIndex) {
        this.field = field;
        this.lookback = lookback;
        this.bars = bars;
        this.currentIndex = currentIndex;
    }

    @Override
    public void roll(Bar latest) {
        this.currentIndex = latest.index;
    }

    @Override
    public Double getValue() {
        int targetIndex = currentIndex - lookback;

        if (targetIndex < 0) {
            throw new RuntimeException("Cannot access bar " + lookback + " bars back from current position");
        }

        Bar targetBar = bars.get(targetIndex);

        return switch (field) {
            case OPEN -> targetBar.open;
            case HIGH -> targetBar.high;
            case LOW -> targetBar.low;
            case CLOSE -> targetBar.close;
            case VOLUME -> (double) targetBar.volume;
        };
    }

    public OhlcvField getField() {
        return field;
    }

    public int getLookback() {
        return lookback;
    }

    public ValueAccumulator<Double> copy() {
        return new OhlcvValueAccumulator(field, lookback, bars, currentIndex);
    }
}

