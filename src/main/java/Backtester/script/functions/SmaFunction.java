package Backtester.script.functions;

import Backtester.objects.Bar;
import Backtester.objects.valueaccumulator.SmaCalculator;
import Backtester.objects.valueaccumulator.key.SmaKey;
import Backtester.script.functions.result.NonVoidScriptFunctionResult;
import Backtester.script.functions.result.ScriptFunctionResult;
import Backtester.script.statements.expressions.FunctionSignatureProperties;
import Backtester.script.statements.expressions.Literal;
import Backtester.strategies.RunContext;

import java.util.LinkedList;
import java.util.List;

public class SmaFunction implements ScriptFunction {

    public static final String FUNCTION_NAME = "sma";
    public static final int EXPECTED_ARGUMENTS = 1; // days

    @Override
    public ScriptFunctionResult execute(List<Object> args, RunContext runContext) {
        int days = getDays(args);
        SmaCalculator calculator;
        SmaKey key = new SmaKey(days);
        if (runContext.valueAccumulatorCache.contains(key)) {
            calculator = (SmaCalculator) runContext.valueAccumulatorCache.getValueAccumulator(key);
        } else {
            List<Bar> initialValues = runContext.lookbackBars;
            calculator = new SmaCalculator(days, initialValues);
            runContext.valueAccumulatorCache.put(key, calculator);
        }
        return new SmaFunctionResult(calculator);
    }

    public List<Bar> getLastNDays(List<Bar> bars, int days, int index) {
        if (days > index + 1) {
            throw new RuntimeException("Can't look back further than specified interval");
        }
        List <Bar> result = new LinkedList<>();
        for (int i = index + 1 - days;  i <= index; i++) {
            result.add(bars.get(i));
        }
        return result;
    }

    public static FunctionSignatureProperties getSignatureProperties() {
        return new FunctionSignatureProperties(EXPECTED_ARGUMENTS, EXPECTED_ARGUMENTS);
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

