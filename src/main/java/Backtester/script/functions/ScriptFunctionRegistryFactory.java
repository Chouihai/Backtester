package Backtester.script.functions;

import Backtester.caches.BarCache;
import Backtester.caches.OrderCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.valueaccumulator.ValueAccumulatorFactory;
import Backtester.script.functions.ohlcv.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;

import java.util.Set;

/**
 * Looks at the function names mentioned in the script and create a ScriptFunctionRegistry
 */
@Singleton
public class ScriptFunctionRegistryFactory {

    private final ValueAccumulatorCache valueAccumulatorCache;
    private final ValueAccumulatorFactory valueAccumulatorFactory;
    private final OrderCache orderCache;
    private final BarCache barCache;
    private final Logger logger = null;


    @Inject()
    public ScriptFunctionRegistryFactory(OrderCache orderCache,
                                         ValueAccumulatorCache valueAccumulatorCache,
                                         ValueAccumulatorFactory valueAccumulatorFactory,
                                         BarCache barCache) {
        this.orderCache = orderCache;
        this.valueAccumulatorCache = valueAccumulatorCache;
        this.valueAccumulatorFactory = valueAccumulatorFactory;
        this.barCache = barCache;
    }

    public ScriptFunctionRegistry createRegistry(Set<String> functionNames) {
        ScriptFunctionRegistry registry = new ScriptFunctionRegistry();
        
        if (functionNames.contains(CreateOrderFn.FUNCTION_NAME)) {
            CreateOrderFn fn = new CreateOrderFn(orderCache);
            registry.register(CreateOrderFn.FUNCTION_NAME, fn);
        }
        
        if (functionNames.contains(SmaFunction.FUNCTION_NAME)) {
            SmaFunction fn = new SmaFunction(valueAccumulatorCache, valueAccumulatorFactory);
            registry.register(SmaFunction.FUNCTION_NAME, fn);
        }
        
        if (functionNames.contains(CrossoverFn.FUNCTION_NAME)) {
            CrossoverFn fn = new CrossoverFn(valueAccumulatorCache);
            registry.register(CrossoverFn.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(CloseFunction.FUNCTION_NAME)) {
            CloseFunction fn = new CloseFunction(valueAccumulatorCache, barCache);
            registry.register(CloseFunction.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(OpenFunction.FUNCTION_NAME)) {
            OpenFunction fn = new OpenFunction(valueAccumulatorCache, barCache);
            registry.register(OpenFunction.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(HighFunction.FUNCTION_NAME)) {
            HighFunction fn = new HighFunction(valueAccumulatorCache, barCache);
            registry.register(HighFunction.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(LowFunction.FUNCTION_NAME)) {
            LowFunction fn = new LowFunction(valueAccumulatorCache, barCache);
            registry.register(LowFunction.FUNCTION_NAME, fn);
        }

        if (functionNames.contains(VolumeFunction.FUNCTION_NAME)) {
            VolumeFunction fn = new VolumeFunction(valueAccumulatorCache, barCache);
            registry.register(VolumeFunction.FUNCTION_NAME, fn);
        }
        
        return registry;
    }
}

