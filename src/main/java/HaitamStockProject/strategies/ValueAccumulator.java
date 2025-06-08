package HaitamStockProject.strategies;

import HaitamStockProject.objects.Bar;

public interface ValueAccumulator {

    void addValue(Bar latest);
}
