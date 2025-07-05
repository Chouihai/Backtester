package Backtester.objects.valueaccumulator;

import Backtester.objects.Bar;

public interface ValueAccumulator<T> {

    void roll(Bar latest);

    T getValue();

    ValueAccumulator<T> copy();
}
