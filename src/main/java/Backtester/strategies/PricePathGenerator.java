package Backtester.strategies;

import Backtester.objects.Bar;

import java.util.List;

/**
 * Abstraction for generating synthetic price paths from a base series.
 */
public interface PricePathGenerator {
    /**
     * Generate a permuted path of the base bars using the provided seed.
     */
    List<Bar> generate(long seed);
}

