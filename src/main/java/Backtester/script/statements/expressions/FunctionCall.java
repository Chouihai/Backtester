package Backtester.script.statements.expressions;

import java.util.List;

public class FunctionCall extends Expression {
    public final String functionName;
    public final List<Expression> arguments;

    public FunctionCall(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
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
