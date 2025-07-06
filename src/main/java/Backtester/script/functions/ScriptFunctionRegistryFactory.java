package Backtester.script.functions;

import Backtester.caches.OrderCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.Bar;
import Backtester.objects.valueaccumulator.*;
import Backtester.script.statements.expressions.FunctionCall;
import com.google.inject.Inject;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;

/**
 * Looks at the function names mentioned in the script and create a ScriptFunctionRegistry
 */
public class ScriptFunctionRegistryFactory {

    private final ValueAccumulatorCache valueAccumulatorCache;
    private final ValueAccumulatorFactory valueAccumulatorFactory;
    private final OrderCache orderCache;
    private final Logger logger = null; // TODO inject this later


    @Inject()
    public ScriptFunctionRegistryFactory(OrderCache orderCache,
                                         ValueAccumulatorCache valueAccumulatorCache,
                                         ValueAccumulatorFactory valueAccumulatorFactory) {
        this.orderCache = orderCache;
        this.valueAccumulatorCache = valueAccumulatorCache;
        this.valueAccumulatorFactory = valueAccumulatorFactory;
    }

    public ScriptFunctionRegistry createRegistry(Map<String, Set<FunctionCall>> functionCalls) {
        ScriptFunctionRegistry registry = new ScriptFunctionRegistry();
        if (functionCalls.containsKey("createOrder")) {
            CreateOrderFn fn = new CreateOrderFn(orderCache);
            registry.register("createOrder", fn);
        }
        if (functionCalls.containsKey("sma")) {
            SmaFunction fn = new SmaFunction(valueAccumulatorCache, valueAccumulatorFactory);
            registry.register("sma", fn);
        }
        if (functionCalls.containsKey("crossover")) {
            CrossoverFn fn = new CrossoverFn(valueAccumulatorCache);
            registry.register("crossover", fn);
        }
        return registry;
    }
}

