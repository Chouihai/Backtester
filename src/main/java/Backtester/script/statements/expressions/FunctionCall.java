package Backtester.script.statements.expressions;

import Backtester.script.functions.CreateOrderFn;
import Backtester.script.functions.SmaFunction;
import Backtester.script.functions.CrossoverFn;
import Backtester.script.functions.CloseOrderFn;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionCall extends Expression {
    public final String functionName;
    public final List<Expression> arguments;

    // Static map of function names to their expected argument counts
    private static final Map<String, Integer> FUNCTION_SIGNATURES = new HashMap<>();
    
    static {
        FUNCTION_SIGNATURES.put(CreateOrderFn.FUNCTION_NAME, CreateOrderFn.EXPECTED_ARGUMENTS);
        FUNCTION_SIGNATURES.put(SmaFunction.FUNCTION_NAME, SmaFunction.EXPECTED_ARGUMENTS);
        FUNCTION_SIGNATURES.put(CrossoverFn.FUNCTION_NAME, CrossoverFn.EXPECTED_ARGUMENTS);
        FUNCTION_SIGNATURES.put(CloseOrderFn.FUNCTION_NAME, CloseOrderFn.EXPECTED_ARGUMENTS);
    }

    public FunctionCall(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public static void validateArguments(String functionName, int actualArgs) {
        if (!FUNCTION_SIGNATURES.containsKey(functionName)) {
            throw new RuntimeException("Unknown function '" + functionName + "'");
        }
        
        int expectedArgs = FUNCTION_SIGNATURES.get(functionName);
        if (actualArgs != expectedArgs) {
            throw new RuntimeException("Function '" + functionName + "' expects " + expectedArgs + 
                                     " arguments but got " + actualArgs);
        }
    }

    @Override
    public boolean isFunctionCall() {
        return true;
    }

    public int argumentSize() {
        return arguments.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.size(); i++) {
            sb.append(arguments.get(i));
            if (i < arguments.size() - 1) sb.append(",");
        }
        String result = sb.toString();
        return functionName + "(" + result + ")";
    }
}
