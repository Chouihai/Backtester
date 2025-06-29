package HaitamStockProject.script.functions.result;

import HaitamStockProject.script.statements.expressions.Literal;

public interface NonVoidScriptFunctionResult extends ScriptFunctionResult {

    Literal getValue();
}
