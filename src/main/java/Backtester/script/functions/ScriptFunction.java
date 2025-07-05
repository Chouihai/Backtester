package Backtester.script.functions;

import Backtester.script.EvaluationContext;
import Backtester.script.functions.result.ScriptFunctionResult;

import java.util.List;

public interface ScriptFunction {

    ScriptFunctionResult execute(List<Object> args, EvaluationContext context);
}
