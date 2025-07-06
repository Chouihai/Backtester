package Backtester.script.functions;

import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.valueaccumulator.ValueAccumulatorFactory;
import Backtester.objects.valueaccumulator.ValueAccumulatorType;
import Backtester.objects.valueaccumulator.key.SmaKey;
import Backtester.script.EvaluationContext;
import Backtester.script.functions.result.NonVoidScriptFunctionResult;
import Backtester.script.functions.result.ScriptFunctionResult;
import Backtester.script.statements.expressions.Literal;
import Backtester.objects.valueaccumulator.SmaCalculator;

import java.util.List;

public class SmaFunction implements ScriptFunction {

    private final ValueAccumulatorCache cache;
    private final ValueAccumulatorFactory factory;

    public SmaFunction(ValueAccumulatorCache cache,
                       ValueAccumulatorFactory factory) {
        this.cache = cache;
        this.factory = factory;
    }

    @Override
    public ScriptFunctionResult execute(List<Object> args, EvaluationContext context) {
        int days = getDays(args);

        SmaCalculator calculator;
        SmaKey key = new SmaKey(days);
        if (cache.contains(key)) {
            calculator = (SmaCalculator) cache.getValueAccumulator(key);
        } else {
            calculator = (SmaCalculator) factory.create(ValueAccumulatorType.SMA, days, context.currentBarIndex());
            cache.put(key, calculator);
        }
        return new SmaFunctionResult(calculator);
    }

    private int getDays(List<Object> args) {
        if (args.size() != 1) throw new IllegalArgumentException("Wrong amount of arguments for function sma"); // TODO: smarter logging
        return Integer.parseInt(args.getFirst().toString());
    }
}

class SmaFunctionResult implements NonVoidScriptFunctionResult {

    private final SmaCalculator calculator;

    public SmaFunctionResult(SmaCalculator smaCalculator) {
        this.calculator = smaCalculator;
    }

    public Literal getValue() {
        return new Literal(calculator);
    }
}

