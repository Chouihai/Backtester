package Backtester.objects.valueaccumulator.key;

import Backtester.objects.valueaccumulator.ValueAccumulatorType;

import java.util.Objects;

public class SmaKey implements ValueAccumulatorKey {
    private final int length;

    public SmaKey(int length) {
        this.length = length;
    }

    public ValueAccumulatorType getValueAccumulatorType() {
        return ValueAccumulatorType.SMA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmaKey that = (SmaKey) o;
        return this.length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(length, ValueAccumulatorType.SMA);
    }
}
