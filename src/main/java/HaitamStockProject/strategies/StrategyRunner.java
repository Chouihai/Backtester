package HaitamStockProject.strategies;

import HaitamStockProject.backtester.caches.BacktestRunValueAccumulatorCache;
import HaitamStockProject.objects.Bar;
import HaitamStockProject.script.EvaluationContext;
import HaitamStockProject.script.ScriptAnalyzer;
import HaitamStockProject.script.functions.ScriptFunctionRegistry;
import HaitamStockProject.script.functions.ScriptFunctionRegistryFactory;
import HaitamStockProject.script.ScriptEvaluator;
import com.google.inject.Inject;

import java.util.List;

public class StrategyRunner {

    private ScriptEvaluator evaluator;
    private final ScriptFunctionRegistryFactory scriptFunctionRegistryFactory;
    private final BacktestRunValueAccumulatorCache backtestRunValueAccumulatorCache;

    @Inject()
    public StrategyRunner(ScriptFunctionRegistryFactory scriptFunctionRegistryFactory,
                          BacktestRunValueAccumulatorCache backtestRunValueAccumulatorCache) {
        this.scriptFunctionRegistryFactory = scriptFunctionRegistryFactory;
        this.backtestRunValueAccumulatorCache = backtestRunValueAccumulatorCache;
    }

    public void initialize(String strategy, List<Bar> initialValues) {
        ScriptAnalyzer scriptAnalyzer = new ScriptAnalyzer(strategy);
        ScriptFunctionRegistry registry = scriptFunctionRegistryFactory.createRegistry(scriptAnalyzer.getAllFunctionsUsed());
        this.evaluator = new ScriptEvaluator(strategy, registry);
        this.backtestRunValueAccumulatorCache.intialize(initialValues);
    }

    void roll(Bar bar) {
        this.backtestRunValueAccumulatorCache.roll(bar);
        evaluator.evaluate(new EvaluationContext(bar));
        // Check the orderCache for positions that need to be executed and EXECUTE THEM
    }
}
