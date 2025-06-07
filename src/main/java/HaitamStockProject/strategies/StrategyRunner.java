package HaitamStockProject.strategies;

import HaitamStockProject.objects.Bar;
import HaitamStockProject.script.ScriptEvaluator;

public class StrategyRunner {

    private final ScriptEvaluator evaluator;
    private final String strategy;

    public StrategyRunner(String strategy, ScriptEvaluator evaluator) {
        this.strategy = strategy;
        // Scan strategy for all functions and identifiers
        // Create what we need to add to our context based on what's in the strategy
        // Ex: if it has the SMA function, we'll need an SMA calculator (with some id)
        this.evaluator = evaluator;
    }

    void next(Bar bar) {}
}
