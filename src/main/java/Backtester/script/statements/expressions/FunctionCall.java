package Backtester.script.statements.expressions;

import Backtester.script.functions.CloseOrderFn;
import Backtester.script.functions.CreateOrderFn;
import Backtester.script.functions.CrossoverFn;
import Backtester.script.functions.SmaFunction;
import Backtester.script.functions.ohlcv.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionCall extends Expression {
    public final String functionName;
    public final List<Expression> arguments;

    private static final Map<String, FunctionSignatureProperties> FUNCTION_SIGNATURES = new HashMap<>();
    
    static {
        FUNCTION_SIGNATURES.put(CreateOrderFn.FUNCTION_NAME, CreateOrderFn.getSignatureProperties());
        FUNCTION_SIGNATURES.put(SmaFunction.FUNCTION_NAME, SmaFunction.getSignatureProperties());
        FUNCTION_SIGNATURES.put(CrossoverFn.FUNCTION_NAME, CrossoverFn.getSignatureProperties());
        FUNCTION_SIGNATURES.put(CloseOrderFn.FUNCTION_NAME, CloseOrderFn.getSignatureProperties());
        FUNCTION_SIGNATURES.put(CloseFunction.FUNCTION_NAME, CloseFunction.getSignatureProperties());
        FUNCTION_SIGNATURES.put(OpenFunction.FUNCTION_NAME, OpenFunction.getSignatureProperties());
        FUNCTION_SIGNATURES.put(HighFunction.FUNCTION_NAME, HighFunction.getSignatureProperties());
        FUNCTION_SIGNATURES.put(LowFunction.FUNCTION_NAME, LowFunction.getSignatureProperties());
        FUNCTION_SIGNATURES.put(VolumeFunction.FUNCTION_NAME, VolumeFunction.getSignatureProperties());
    }

    public FunctionCall(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public static void validateArguments(String functionName, int actualArgs) {
        if (!FUNCTION_SIGNATURES.containsKey(functionName)) {
            throw new RuntimeException("Unknown function '" + functionName + "'");
        }
        
        FunctionSignatureProperties signatureProperites = FUNCTION_SIGNATURES.get(functionName);
        if (actualArgs < signatureProperites.minimumArguments() || actualArgs > signatureProperites.maximumArguments()) {
            String message;
            if (signatureProperites.maximumArguments() == signatureProperites.minimumArguments()) {
                message = "Function '" + functionName + "' expects " + signatureProperites.minimumArguments() +
                        " arguments but got " + actualArgs;
            } else {
                message = "Function '" + functionName + "' expects no less than " + signatureProperites.minimumArguments() +
                        " arguments, and no more than " + signatureProperites.maximumArguments() + " but got " + actualArgs;
            }
            throw new RuntimeException(message);
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
