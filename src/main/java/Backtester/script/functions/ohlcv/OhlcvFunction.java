package Backtester.script.functions.ohlcv;

import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.valueaccumulator.OhlcvField;
import Backtester.objects.valueaccumulator.OhlcvValueAccumulator;
import Backtester.objects.valueaccumulator.key.OhlcvKey;
import Backtester.script.functions.ScriptFunction;
import Backtester.script.functions.result.NonVoidScriptFunctionResult;
import Backtester.script.functions.result.ScriptFunctionResult;
import Backtester.script.statements.expressions.FunctionSignatureProperties;
import Backtester.script.statements.expressions.Literal;
import Backtester.strategies.RunContext;

import java.util.List;

public abstract class OhlcvFunction implements ScriptFunction {
    public static final int MINIMUM_ARGUMENTS = 0;
    public static final int MAXIMUM_ARGUMENTS = 1;

    public abstract OhlcvField getField();

    public abstract String getFunctionName();

    public static FunctionSignatureProperties getSignatureProperties() {
        return new FunctionSignatureProperties(MINIMUM_ARGUMENTS, MAXIMUM_ARGUMENTS);
    }

    public ScriptFunctionResult execute(List<Object> args, RunContext runContext) {
        int lookback = parseLookback(args);

        OhlcvKey key = new OhlcvKey(getField(), lookback);

        OhlcvValueAccumulator accumulator;
        ValueAccumulatorCache cache = runContext.valueAccumulatorCache;
        if (cache.contains(key)) {
            accumulator = (OhlcvValueAccumulator) cache.getValueAccumulator(key);
        } else {
            accumulator = new OhlcvValueAccumulator(getField(), lookback, runContext.bars, runContext.currentIndex);
            cache.put(key, accumulator);
        }

        return new OhlcvFunctionResult(accumulator);
    }

    private int parseLookback(List<Object> args) {
        if (args.size() > 1) {
            throw new IllegalArgumentException(getFunctionName() + " function expects 0 or 1 arguments: [lookback]");
        }

        if (args.isEmpty()) {
            return 0;
        }

        try {
            int lookback = Integer.parseInt(args.get(0).toString());
            if (lookback < 0) {
                throw new IllegalArgumentException("Lookback must be non-negative");
            }
            return lookback;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Lookback must be an integer");
        }
    }
}

class OhlcvFunctionResult implements NonVoidScriptFunctionResult {

    private final OhlcvValueAccumulator result;

    public OhlcvFunctionResult(OhlcvValueAccumulator result) {
        this.result = result;
    }

    public Literal getValue() {
        return new Literal(result);
    }
}
