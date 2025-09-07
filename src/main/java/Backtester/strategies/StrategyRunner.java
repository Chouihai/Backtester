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
                          List<Bar> lookbackBars,
                          String script,
                          Logger logger) {
        this.bars = bars;
        this.logger = logger;
        this.runContext = new RunContext(bars, lookbackBars);
        this.evaluator = new ScriptEvaluator(script);
    }

    public RunResult run(double initialCapital) {
        int n = bars.size();
        evaluator.evaluate(runContext); // This is necessary, can't remember why but not to be touched
        runContext.positionManager.setInitialCapital(initialCapital);
        for (int i = 0; i < n; i++) {
            Bar bar = bars.get(i);
            roll(bar);
        }
        PositionManager pm = runContext.positionManager;
        
        List<Double> equity = pm.getEquitySeries();
        strategyEquity.clear();
        strategyEquity.addAll(equity);

        double years = 0.0;
        if (!bars.isEmpty()) {
            var d0 = bars.get(0).date;
            var dT = bars.get(bars.size() - 1).date;
            long days = java.time.temporal.ChronoUnit.DAYS.between(d0, dT);
            years = (days > 0) ? (days / 365.25) : (bars.size() / 252.0);
        }

        return new RunResult(
                pm.allTrades(),
                pm.netProfit(),
                pm.grossProfit(),
                pm.grossLoss(),
                pm.sharpeRatio(),
                pm.sortinoRatio(),
                pm.annualizedVolatility(),
                pm.cagr(years),
                pm.calmar(years),
                pm.openPnL(),
                pm.maxDrawdown(),
                pm.maxRunUp(),
                runContext.bars.getLast(),
                strategyEquity
        );
    }

    /**
     * First we run anything we need to at open (Fill orders)
     * Then we run the script... I think
     */
    private void roll(Bar bar) {
        runContext.roll(bar);
        evaluator.evaluate(runContext);
    }
}
