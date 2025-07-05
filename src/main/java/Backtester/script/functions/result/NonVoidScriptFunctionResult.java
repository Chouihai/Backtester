package Backtester.script.functions.result;

import Backtester.script.statements.expressions.Literal;

public interface NonVoidScriptFunctionResult extends ScriptFunctionResult {

    Literal getValue();
}
