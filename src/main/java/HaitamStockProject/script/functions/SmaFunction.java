package HaitamStockProject.script.functions;

import HaitamStockProject.backtester.caches.BacktestRunValueAccumulatorCache;
import HaitamStockProject.script.EvaluationContext;
import HaitamStockProject.strategies.SmaCalculator;

import java.util.List;

public class SmaFunction implements ScriptFunction {

    private final BacktestRunValueAccumulatorCache cache;

    public SmaFunction(BacktestRunValueAccumulatorCache cache) {
        this.cache = cache;
    }

    @Override
    public ScriptFunctionResult execute(List<Object> args, EvaluationContext context) {
        int days = getDays(args);
        SmaCalculator calculator = (SmaCalculator) cache.getValueAccumulator("sma" + days);
        return new SmaFunctionResult(calculator.getAverage());
    }

    private int getDays(List<Object> args) {
        if (args.size() != 1) throw new IllegalArgumentException("Wrong amount of arguments for function sma");
        return Integer.parseInt(args.getFirst().toString());
    }
}

class SmaFunctionResult extends NonVoidScriptFunctionResult<Double> {

    private final double sma;

    public SmaFunctionResult(double sma) {
        this.sma = sma;
    }

    public Double getValue() {
        return sma;
    }
}
