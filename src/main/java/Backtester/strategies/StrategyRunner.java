package Backtester.strategies;

import Backtester.objects.Bar;
import Backtester.script.ScriptEvaluator;
import Backtester.trades.PositionManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class StrategyRunner {

    private final ScriptEvaluator evaluator;
    private final List<Bar> bars;
    private final Logger logger;
    private final List<Double> strategyEquity = new ArrayList<>();
    private final RunContext runContext;

    public StrategyRunner(List<Bar> bars,
                          String script,
                          Logger logger) {
        this.bars = bars;
        this.logger = logger;
        this.evaluator = new ScriptEvaluator(script);
        this.runContext = new RunContext(bars);
    }

    public RunResult run(double initialCapital) {
        int n = bars.size();
        logger.info("Running strategy across " + n  + " cached bars");
        evaluator.evaluate(runContext);
        for (int i = 1; i < n; i++) {
            Bar bar = bars.get(i);
            roll(bar);

            double realized = runContext.positionManager.netProfit();
            double unrealized = runContext.positionManager.openPnL();
            double equity = initialCapital + realized + unrealized;
            strategyEquity.add(equity);
        }
        logger.info("Finished running strategy");
        PositionManager pm = runContext.positionManager;
        return new RunResult(pm.allTrades(), pm.netProfit(), pm.grossProfit(), pm.grossLoss(), pm.sharpeRatio(), pm.openPnL(), pm.maxDrawdown(),
                pm.maxRunUp(), runContext.bars.getLast(), strategyEquity);
    }

    /**
     * First we run anything we need to at open (Fill orders)
     * Then we run the script... I think
     */
    public void roll(Bar bar) {
        runContext.roll(bar);
        evaluator.evaluate(runContext);
    }
}
