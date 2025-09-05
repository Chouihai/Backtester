package Backtester.script.functions;

import Backtester.script.functions.result.ScriptFunctionResult;
import Backtester.strategies.RunContext;

import java.util.List;

public interface ScriptFunction {

    ScriptFunctionResult execute(List<Object> args, RunContext runContext);
}
