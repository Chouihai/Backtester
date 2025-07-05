package Backtester.objects.valueaccumulator.key;

import Backtester.objects.valueaccumulator.ValueAccumulatorType;

import java.util.Objects;

public class CrossoverKey implements ValueAccumulatorKey {
    private final ValueAccumulatorKey va1;
    private final ValueAccumulatorKey va2;

    public CrossoverKey(ValueAccumulatorKey va1, ValueAccumulatorKey va2) {
        this.va1 = va1;
        this.va2 = va2;
    }

    public ValueAccumulatorType getValueAccumulatorType() {
        return ValueAccumulatorType.Crossover;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrossoverKey that = (CrossoverKey) o;
        return Objects.equals(va1, that.va1) && Objects.equals(va2, that.va2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(va1, va2);
    }
}
