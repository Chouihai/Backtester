package HaitamStockProject.script.functions;

public abstract class NonVoidScriptFunctionResult<T> extends ScriptFunctionResult {

    public abstract T getValue();
}
