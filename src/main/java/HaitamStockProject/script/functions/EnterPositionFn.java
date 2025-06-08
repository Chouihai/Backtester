package HaitamStockProject.script.functions;

import HaitamStockProject.backtester.caches.BacktestRunOrderCache;
import HaitamStockProject.objects.Order;
import HaitamStockProject.script.EvaluationContext;
import HaitamStockProject.services.BusinessDayService;
import HaitamStockProject.services.OrderIdGeneratorService;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;

public class EnterPositionFn implements ScriptFunction {

    private final Logger logger;
    private final int ARGUMENTS_SIZE = 4; // Later on might want to have required vs optional args
    private final BusinessDayService businessDayService;
    private final OrderIdGeneratorService orderIdGeneratorService;
    private final BacktestRunOrderCache orderCache;

    // Inject positionCache and orderCache
    public EnterPositionFn(Logger logger,
                           BusinessDayService businessDayService,
                           BacktestRunOrderCache orderCache,
                           OrderIdGeneratorService orderIdGeneratorService) {
        this.logger = logger;
        this.businessDayService = businessDayService;
        this.orderCache = orderCache;
        this.orderIdGeneratorService = orderIdGeneratorService;
    }

    @Override
    public ScriptFunctionResult execute(List<Object> args, EvaluationContext context) {
        EnterPositionFnArguments arguments = validateArgs(args);
        double open = context.currentBar().getOpen();
        LocalDate date = context.currentBar().getDate();
        Order newOrder = new Order("", arguments.getSymbol(), open, arguments.getQuantity(), date, businessDayService.nextBusinessDay(date));
        String orderId = orderIdGeneratorService.generateId(newOrder);
        Order orderWithId = newOrder.withNewId(orderId);
        orderCache.addOrder(orderWithId);
        return new VoidScriptFunctionResult();
    }

    // TODO Later on add validation instead of throwing Exceptions
    private EnterPositionFnArguments validateArgs(List<Object> args) {
        if (args.size() != ARGUMENTS_SIZE) throw new IllegalArgumentException("Bad arguments size in EnterPosition");
        String symbol = args.get(0).toString();
        int quantity = Integer.parseInt(args.get(1).toString());
        return new EnterPositionFnArguments(symbol, quantity);
    }
}

class EnterPositionFnArguments {

    private final String symbol;
    private final int quantity;

    public int getQuantity() {
        return quantity;
    }

    public String getSymbol() {
        return symbol;
    }

    EnterPositionFnArguments(String symbol, int open) {
        this.symbol = symbol;
        this.quantity = open;
    }
}
