package HaitamStockProject.script.functions;

import HaitamStockProject.script.EvaluationContext;
import HaitamStockProject.script.functions.result.ScriptFunctionResult;

import java.util.List;

public interface ScriptFunction {

    ScriptFunctionResult execute(List<Object> args, EvaluationContext context);
}
