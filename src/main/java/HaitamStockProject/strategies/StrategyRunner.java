package HaitamStockProject.strategies;

import HaitamStockProject.backtester.caches.ValueAccumulatorCache;
import HaitamStockProject.objects.Bar;
import HaitamStockProject.objects.CompiledScript;
import HaitamStockProject.script.EvaluationContext;
import HaitamStockProject.script.functions.ScriptFunctionRegistry;
import HaitamStockProject.script.functions.ScriptFunctionRegistryFactory;
import HaitamStockProject.script.ScriptEvaluator;
import HaitamStockProject.script.tokens.Parser;
import com.google.inject.Inject;

public class StrategyRunner {

    private ScriptEvaluator evaluator;
    private final ScriptFunctionRegistryFactory scriptFunctionRegistryFactory;
    private final ValueAccumulatorCache valueAccumulatorCache;
    private final PositionManager positionManager;

    @Inject()
    public StrategyRunner(ScriptFunctionRegistryFactory scriptFunctionRegistryFactory,
                          ValueAccumulatorCache valueAccumulatorCache,
                          PositionManager positionManager) {
        this.scriptFunctionRegistryFactory = scriptFunctionRegistryFactory;
        this.valueAccumulatorCache = valueAccumulatorCache;
        this.positionManager = positionManager;
    }

    /**
     * Get all functions and value accumulators needed
     */
    public void initialize(String strategy, String symbol, Bar initialBar) {
        CompiledScript compiled = new Parser().parse(strategy);
        ScriptFunctionRegistry registry = scriptFunctionRegistryFactory.createRegistry(compiled.functionCalls(), initialBar);
        this.evaluator = new ScriptEvaluator(compiled, registry, valueAccumulatorCache);
        this.evaluator.evaluate(new EvaluationContext(initialBar));
    }

    /**
     * First we run anything we need to at open (Fill orders)
     * Then we run the script... I think
     */
    public void roll(Bar bar) {
        this.valueAccumulatorCache.roll(bar);
        this.positionManager.roll(bar);
        evaluator.evaluate(new EvaluationContext(bar));
    }

    public double netProfit() {
        return positionManager.netProfit();
    }

    public double grossProfit() {
        return positionManager.grossProfit();
    }

    public double grossLoss() {
        return positionManager.grossLoss();
    }
}
