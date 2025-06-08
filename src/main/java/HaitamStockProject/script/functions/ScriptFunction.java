package HaitamStockProject.script.functions;

import HaitamStockProject.script.EvaluationContext;

import java.util.List;

public interface ScriptFunction {

    ScriptFunctionResult execute(List<Object> args, EvaluationContext context);
}
