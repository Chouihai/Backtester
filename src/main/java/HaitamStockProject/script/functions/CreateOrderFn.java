package HaitamStockProject.script.functions;

import HaitamStockProject.backtester.caches.OrderCache;
import HaitamStockProject.objects.order.Order;
import HaitamStockProject.objects.order.OrderSide;
import HaitamStockProject.objects.order.OrderStatus;
import HaitamStockProject.objects.order.OrderType;
import HaitamStockProject.script.EvaluationContext;
import HaitamStockProject.script.functions.result.ScriptFunctionResult;
import HaitamStockProject.script.functions.result.VoidScriptFunctionResult;
import HaitamStockProject.services.BusinessDayService;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateOrderFn implements ScriptFunction {

    private final Logger logger = null; // TODO: add logger later
    private final static int MINIMUM_ARGUMENTS_SIZE = 2; // Later on might want to have required vs optional args
    private final static int MAXIMUM_ARGUMENTS_SIZE = 3; // Later on might want to have required vs optional args
    private final OrderCache orderCache;
    private final BusinessDayService businessDayService;
    private final String symbol = "AAPL"; // TODO Inject this later
    private final AtomicInteger idGenerator = new AtomicInteger(1); // Orders will exist entirely in memory

    // Use injection later
    public CreateOrderFn(OrderCache orderCache, BusinessDayService businessDayService) {
        this.orderCache = orderCache;
        this.businessDayService = businessDayService;
    }

    @Override
    public ScriptFunctionResult execute(List<Object> args, EvaluationContext context) {
        CreateOrderFnArguments arguments = validateArgs(args);
        LocalDate date = businessDayService.nextBusinessDay(context.currentBar().getDate());
        Order newOrder = new Order(idGenerator.getAndIncrement(), symbol, OrderStatus.SUBMITTED, OrderSide.BUY, OrderType.Market, 0.0, arguments.quantity(), date, arguments.name());
        orderCache.addOrder(newOrder);
        return new VoidScriptFunctionResult();
    }

    // TODO Later on add validation instead of throwing Exceptions.
    private CreateOrderFnArguments validateArgs(List<Object> args) {
        if (args.size() > MAXIMUM_ARGUMENTS_SIZE || args.size() < MINIMUM_ARGUMENTS_SIZE)
            throw new IllegalArgumentException("Bad arguments size in EnterPosition");
        Object nameArg = args.get(0);
        String name;
        if (nameArg == null) {
            name = "";
        } else name = nameArg.toString();
        boolean isBuy = Boolean.getBoolean(args.get(1).toString());
        OrderSide side = (isBuy) ? OrderSide.BUY : OrderSide.SELL;
        int quantity = Integer.parseInt(args.get(2).toString());
        return new CreateOrderFnArguments(name, side, quantity);
    }
}

record CreateOrderFnArguments(String name, OrderSide side, int quantity) {}