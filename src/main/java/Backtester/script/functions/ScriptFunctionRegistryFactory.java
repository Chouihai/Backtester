package Backtester.script.functions;

import Backtester.script.functions.ohlcv.*;

import java.util.Set;

public class ScriptFunctionRegistryFactory {

    public static ScriptFunctionRegistry createRegistry(Set<String> functionNames) {
        ScriptFunctionRegistry registry = new ScriptFunctionRegistry();

        if (functionNames.contains(CreateOrderFn.FUNCTION_NAME)) {
            CreateOrderFn fn = new CreateOrderFn();
            registry.register(CreateOrderFn.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(SmaFunction.FUNCTION_NAME)) {
            SmaFunction fn = new SmaFunction();
            registry.register(SmaFunction.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(CrossoverFn.FUNCTION_NAME)) {
            CrossoverFn fn = new CrossoverFn();
            registry.register(CrossoverFn.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(CloseFunction.FUNCTION_NAME)) {
            CloseFunction fn = new CloseFunction();
            registry.register(CloseFunction.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(OpenFunction.FUNCTION_NAME)) {
            OpenFunction fn = new OpenFunction();
            registry.register(OpenFunction.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(HighFunction.FUNCTION_NAME)) {
            HighFunction fn = new HighFunction();
            registry.register(HighFunction.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(LowFunction.FUNCTION_NAME)) {
            LowFunction fn = new LowFunction();
            registry.register(LowFunction.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(VolumeFunction.FUNCTION_NAME)) {
            VolumeFunction fn = new VolumeFunction();
            registry.register(VolumeFunction.FUNCTION_NAME, fn);
        }

        return registry;
    }
}
