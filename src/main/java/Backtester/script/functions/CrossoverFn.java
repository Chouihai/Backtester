package Backtester.script.functions;

import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.valueaccumulator.CrossoverDetector;
import Backtester.objects.valueaccumulator.ValueAccumulator;
import Backtester.objects.valueaccumulator.key.CrossoverKey;
import Backtester.objects.valueaccumulator.key.ValueAccumulatorKey;
import Backtester.objects.valueaccumulator.key.ValueAccumulatorKeyBuilder;
import Backtester.script.functions.result.NonVoidScriptFunctionResult;
import Backtester.script.functions.result.ScriptFunctionResult;
import Backtester.script.statements.expressions.FunctionSignatureProperties;
import Backtester.script.statements.expressions.Literal;
import Backtester.strategies.RunContext;

import java.util.List;

public class CrossoverFn implements ScriptFunction {

    public static final String FUNCTION_NAME = "crossover";
    public static final int EXPECTED_ARGUMENTS = 2; // valueAccumulator1, valueAccumulator2
    private final ValueAccumulatorKeyBuilder valueAccumulatorKeyBuilder = new ValueAccumulatorKeyBuilder();

    @Override
    public ScriptFunctionResult execute(List<Object> args, RunContext runContext) {
        CrossoverFnArguments fnArgs = checkArgs(args, runContext.valueAccumulatorCache);
        return new CrossoverFnResult(fnArgs.crossoverDetector());
    }

    public static FunctionSignatureProperties getSignatureProperties() {
        return new FunctionSignatureProperties(EXPECTED_ARGUMENTS, EXPECTED_ARGUMENTS);
    }

    private CrossoverFnArguments checkArgs(List<Object> args, ValueAccumulatorCache valueAccumulatorCache) {
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
