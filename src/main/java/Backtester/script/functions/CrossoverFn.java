package Backtester.script.functions;

import Backtester.backtester.caches.ValueAccumulatorCache;
import Backtester.objects.valueaccumulator.CrossoverDetector;
import Backtester.objects.valueaccumulator.ValueAccumulator;
import Backtester.objects.valueaccumulator.key.CrossoverKey;
import Backtester.objects.valueaccumulator.key.ValueAccumulatorKey;
import Backtester.objects.valueaccumulator.key.ValueAccumulatorKeyBuilder;
import Backtester.script.EvaluationContext;
import Backtester.script.functions.result.NonVoidScriptFunctionResult;
import Backtester.script.functions.result.ScriptFunctionResult;
import Backtester.script.statements.expressions.Literal;

import java.util.List;

public class CrossoverFn implements ScriptFunction {

    private final ValueAccumulatorCache valueAccumulatorCache;
    private final ValueAccumulatorKeyBuilder valueAccumulatorKeyBuilder = new ValueAccumulatorKeyBuilder();

    public CrossoverFn(ValueAccumulatorCache valueAccumulatorCache) {
        this.valueAccumulatorCache = valueAccumulatorCache;
    }

    @Override
    public ScriptFunctionResult execute(List<Object> args, EvaluationContext context) {
        CrossoverFnArguments fnArgs = checkArgs(args);
        return new CrossoverFnResult(fnArgs.crossoverDetector());
    }

    private CrossoverFnArguments checkArgs(List<Object> args) {
        ValueAccumulator<Double> arg1 = ((ValueAccumulator<Double>) args.get(0));
        ValueAccumulator<Double> arg2 = ((ValueAccumulator<Double>) args.get(1));
        ValueAccumulatorKey key1 = valueAccumulatorKeyBuilder.build(arg1);
        ValueAccumulatorKey key2 = valueAccumulatorKeyBuilder.build(arg2);
        CrossoverKey crossoverKey = new CrossoverKey(key1, key2);

        CrossoverDetector crossoverDetector;
        if (valueAccumulatorCache.contains(crossoverKey)) {
            crossoverDetector = (CrossoverDetector) valueAccumulatorCache.getValueAccumulator(crossoverKey);
        } else {
            crossoverDetector = new CrossoverDetector(arg1.copy(), arg2.copy());
            valueAccumulatorCache.put(crossoverKey, crossoverDetector);
        }
        return new CrossoverFnArguments(crossoverDetector);
    }
}


record CrossoverFnArguments(CrossoverDetector crossoverDetector) {}

class CrossoverFnResult implements NonVoidScriptFunctionResult {

    private final ValueAccumulator<Boolean> result;

    public CrossoverFnResult(ValueAccumulator<Boolean> result) {
        this.result = result;
    }

    public Literal getValue() {
        return new Literal(result);
    }
}
