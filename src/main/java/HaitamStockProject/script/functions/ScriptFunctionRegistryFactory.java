package HaitamStockProject.script.functions;

import HaitamStockProject.backtester.caches.OrderCache;
import HaitamStockProject.backtester.caches.ValueAccumulatorCache;
import HaitamStockProject.objects.Bar;
import HaitamStockProject.objects.valueaccumulator.*;
import HaitamStockProject.objects.valueaccumulator.key.ValueAccumulatorKeyBuilder;
import HaitamStockProject.script.statements.expressions.FunctionCall;
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
    private final ValueAccumulatorKeyBuilder valueAccumulatorKeyBuilder = new ValueAccumulatorKeyBuilder();
    private final Logger logger = null; // TODO inject this later


    @Inject()
    public ScriptFunctionRegistryFactory(OrderCache orderCache,
                                         ValueAccumulatorCache valueAccumulatorCache,
                                         ValueAccumulatorFactory valueAccumulatorFactory) {
        this.orderCache = orderCache;
        this.valueAccumulatorCache = valueAccumulatorCache;
        this.valueAccumulatorFactory = valueAccumulatorFactory;
    }

    public ScriptFunctionRegistry createRegistry(Map<String, Set<FunctionCall>> functionCalls, Bar startingBar) {
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
//            Set<FunctionCall> calls = functionCalls.get("crossover");
//            for (FunctionCall call: calls) {
//                ValueAccumulator<Double> arg1 = ((ValueAccumulator<Double>) call.arguments.get(0));
//                ValueAccumulator<Double> arg2 = ((ValueAccumulator<Double>) call.arguments.get(1));
//
//                CrossoverDetector crossoverDetector = new CrossoverDetector(arg1.copy(), arg2.copy());
//                ValueAccumulatorKey key1 = valueAccumulatorKeyBuilder.build(arg1);
//                ValueAccumulatorKey key2 = valueAccumulatorKeyBuilder.build(arg2);
//                CrossoverKey key = new CrossoverKey(key1, key2);
//
//                valueAccumulatorCache.put(key, crossoverDetector);
//            }
            CrossoverFn fn = new CrossoverFn(valueAccumulatorCache);
            registry.register("crossover", fn);
        }
        return registry;
    }
}
