package Backtester.objects.valueaccumulator.key;

import Backtester.objects.valueaccumulator.OhlcvField;
import Backtester.objects.valueaccumulator.ValueAccumulatorType;

import java.util.Objects;

public class OhlcvKey implements ValueAccumulatorKey {
    private final OhlcvField field;
    private final int lookback;

    public OhlcvKey(OhlcvField field, int lookback) {
        this.field = field;
        this.lookback = lookback;
    }

    public OhlcvField getField() {
        return field;
    }

    public int getLookback() {
        return lookback;
    }

    public ValueAccumulatorType getValueAccumulatorType() {
        return ValueAccumulatorType.OHLCV;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OhlcvKey that = (OhlcvKey) o;
        return lookback == that.lookback && field == that.field;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, lookback, ValueAccumulatorType.OHLCV);
    }
}