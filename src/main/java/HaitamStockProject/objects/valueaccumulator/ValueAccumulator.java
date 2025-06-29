package HaitamStockProject.objects.valueaccumulator;

import HaitamStockProject.objects.Bar;

public interface ValueAccumulator<T> {

    void roll(Bar latest);

    T getValue();

    ValueAccumulator<T> copy();
}
