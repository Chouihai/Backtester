package Backtester.script.functions;

import Backtester.caches.OrderCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.valueaccumulator.ValueAccumulatorFactory;
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
    private final Logger logger = null;


    @Inject()
    public ScriptFunctionRegistryFactory(OrderCache orderCache,
                                         ValueAccumulatorCache valueAccumulatorCache,
                                         ValueAccumulatorFactory valueAccumulatorFactory) {
        this.orderCache = orderCache;
        this.valueAccumulatorCache = valueAccumulatorCache;
        this.valueAccumulatorFactory = valueAccumulatorFactory;
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
        
        return registry;
    }
}

