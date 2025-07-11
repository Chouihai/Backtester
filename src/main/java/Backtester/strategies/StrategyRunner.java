package Backtester.strategies;

import Backtester.caches.BarCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.Bar;
import Backtester.objects.CompiledScript;
import Backtester.script.EvaluationContext;
import Backtester.script.ScriptEvaluator;
import Backtester.script.functions.ScriptFunctionRegistry;
import Backtester.script.functions.ScriptFunctionRegistryFactory;
import Backtester.script.tokens.Parser;
import com.google.inject.Inject;

import java.time.LocalDate;
import java.util.logging.Logger;

public class StrategyRunner {

    private ScriptEvaluator evaluator;
    private final ScriptFunctionRegistryFactory scriptFunctionRegistryFactory;
    private final ValueAccumulatorCache valueAccumulatorCache;
    private final PositionManager positionManager;
    private final BarCache barCache;
    private final Logger logger;

    @Inject()
    public StrategyRunner(ScriptFunctionRegistryFactory scriptFunctionRegistryFactory,
                          ValueAccumulatorCache valueAccumulatorCache,
                          PositionManager positionManager,
                          Logger logger,
                          BarCache barCache) {
        this.scriptFunctionRegistryFactory = scriptFunctionRegistryFactory;
        this.valueAccumulatorCache = valueAccumulatorCache;
        this.positionManager = positionManager;
        this.barCache = barCache;
        this.logger = logger;
    }

    public void run(String strategy, LocalDate startDate, LocalDate endDate) {
        int initialIndex = barCache.findIndexAfterDate(startDate);
        int endIndex = barCache.findIndexBeforeDate(endDate);
        logger.info("About to start running strategy starting at index " + initialIndex + " and ending at index " + endIndex);
        initialize(strategy, initialIndex);
        int i = initialIndex + 1;
        while (i < endIndex) {
            roll(barCache.get(i));
            i++;
        }
        logger.info("Finished running strategy"); // TODO do a timed log later
    }

    /**
     * Get all functions and value accumulators needed
     */
    public void initialize(String strategy, int startingIndex) {
        positionManager.reset();
        valueAccumulatorCache.reset();
        CompiledScript compiled = new Parser().parse(strategy);
        ScriptFunctionRegistry registry = scriptFunctionRegistryFactory.createRegistry(compiled.functionCalls());
        this.evaluator = new ScriptEvaluator(compiled, registry, valueAccumulatorCache);
        this.evaluator.evaluate(new EvaluationContext(startingIndex));
    }

    /**
     * First we run anything we need to at open (Fill orders)
     * Then we run the script... I think
     */
    public void roll(Bar bar) {
        this.valueAccumulatorCache.roll(bar);
        this.positionManager.roll(bar);
        evaluator.evaluate(new EvaluationContext(bar.index));
    }
}

