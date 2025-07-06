package Backtester.script.functions;

import Backtester.caches.OrderCache;
import Backtester.objects.order.Order;
import Backtester.objects.order.OrderSide;
import Backtester.objects.order.OrderStatus;
import Backtester.objects.order.OrderType;
import Backtester.script.EvaluationContext;
import Backtester.script.functions.result.ScriptFunctionResult;
import Backtester.script.functions.result.VoidScriptFunctionResult;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateOrderFn implements ScriptFunction {

    private final Logger logger = null; // TODO: add logger later
    private final static int MINIMUM_ARGUMENTS_SIZE = 2; // Later on might want to have required vs optional args
    private final static int MAXIMUM_ARGUMENTS_SIZE = 3; // Later on might want to have required vs optional args
    private final OrderCache orderCache;
    private final String symbol = "AAPL"; // TODO Inject this later
    private final AtomicInteger idGenerator = new AtomicInteger(1); // Orders will exist entirely in memory

    // Use injection later
    public CreateOrderFn(OrderCache orderCache) {
        this.orderCache = orderCache;
    }

    @Override
    public ScriptFunctionResult execute(List<Object> args, EvaluationContext context) {
        CreateOrderFnArguments arguments = validateArgs(args);
        Order newOrder = new Order(idGenerator.getAndIncrement(), symbol, OrderStatus.OPEN, arguments.side(), OrderType.Market, 0.0, 0.0, 0.0, arguments.quantity(), null, arguments.name());
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
        boolean isBuy = (Boolean) args.get(1);
        OrderSide side = (isBuy) ? OrderSide.BUY : OrderSide.SELL;
        int quantity = Integer.parseInt(args.get(2).toString());
        if (quantity <= 0) throw new RuntimeException("Negative quantity in CreateOrderFn");
        return new CreateOrderFnArguments(name, side, quantity);
    }
}

record CreateOrderFnArguments(String name, OrderSide side, int quantity) {}
