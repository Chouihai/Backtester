package HaitamStockProject.script.functions;

import HaitamStockProject.backtester.caches.BacktestRunOrderCache;
import HaitamStockProject.backtester.caches.BacktestRunValueAccumulatorCache;
import HaitamStockProject.services.BusinessDayService;
import HaitamStockProject.services.OrderIdGeneratorService;
import com.google.inject.Inject;
import org.slf4j.Logger;

import java.util.Set;

public class ScriptFunctionRegistryFactory {

    private final BusinessDayService businessDayService;
    private final OrderIdGeneratorService orderIdGeneratorService;
    private final BacktestRunValueAccumulatorCache valueAccumulatorCache;
    private final BacktestRunOrderCache orderCache;
    private final Logger logger;


    @Inject()
    public ScriptFunctionRegistryFactory(BusinessDayService businessDayService,
                                         OrderIdGeneratorService orderIdGeneratorService,
                                         BacktestRunOrderCache orderCache,
                                         Logger logger,
                                         BacktestRunValueAccumulatorCache backtestRunValueAccumulatorCache) {
        this.businessDayService = businessDayService;
        this.orderIdGeneratorService = orderIdGeneratorService;
        this.orderCache = orderCache;
        this.logger = logger;
        this.valueAccumulatorCache = backtestRunValueAccumulatorCache;
    }

    public ScriptFunctionRegistry createRegistry(Set<String> functionNames) {
        ScriptFunctionRegistry registry = new ScriptFunctionRegistry();
        if (functionNames.contains("enterPosition")) {
            EnterPositionFn fn = new EnterPositionFn(logger, businessDayService, orderCache, orderIdGeneratorService);
            registry.register("enterPosition", fn);
        } else if (functionNames.contains("sma")) {
            SmaFunction fn = new SmaFunction(valueAccumulatorCache);
            registry.register("sma", fn);
        }
        return registry;
    }
}
