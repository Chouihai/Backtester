package Backtester.script.functions;

import Backtester.caches.OrderCache;
import Backtester.objects.order.Order;
import Backtester.objects.order.OrderSide;
import Backtester.objects.order.OrderStatus;
import Backtester.objects.order.OrderType;
import Backtester.script.EvaluationContext;
import Backtester.script.functions.result.ScriptFunctionResult;
import Backtester.script.functions.result.VoidScriptFunctionResult;
import Backtester.script.statements.expressions.FunctionSignatureProperties;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateOrderFn implements ScriptFunction {

    private final Logger logger = null; // TODO: add logger later
    public static final String FUNCTION_NAME = "createOrder";
    private final static int MINIMUM_ARGUMENTS_SIZE = 3; // name, isBuy, quantity are required
    private final static int MAXIMUM_ARGUMENTS_SIZE = 5; // name, isBuy, quantity, orderType, limitPrice, stopPrice
    private final OrderCache orderCache;
    private final String symbol = "AAPL"; // TODO Inject this later
    private final AtomicInteger idGenerator = new AtomicInteger(1); // Orders will exist entirely in memory

    // Use injection later
    public CreateOrderFn(OrderCache orderCache) {
        this.orderCache = orderCache;
    }

    public ScriptFunctionResult execute(List<Object> args, EvaluationContext context) {
        CreateOrderFnArguments arguments = validateArgs(args);
        Order newOrder = new Order(idGenerator.getAndIncrement(), symbol, OrderStatus.OPEN, arguments.side(), 
                                 arguments.orderType(), arguments.limitPrice(), arguments.stopPrice(), 
                                 0.0, arguments.quantity(), null, arguments.name());
        orderCache.addOrder(newOrder);
        return new VoidScriptFunctionResult();
    }

    public static FunctionSignatureProperties getSignatureProperties() {
        return new FunctionSignatureProperties(MINIMUM_ARGUMENTS_SIZE, MAXIMUM_ARGUMENTS_SIZE);
    }

    // TODO Later on add validation instead of throwing Exceptions.
    private CreateOrderFnArguments validateArgs(List<Object> args) {
        if (args.size() > MAXIMUM_ARGUMENTS_SIZE || args.size() < MINIMUM_ARGUMENTS_SIZE)
            throw new IllegalArgumentException("Bad arguments size in CreateOrderFn. Expected 3-6 arguments: name, isBuy, quantity, [orderType], [limitPrice], [stopPrice]");
        
        Object nameArg = args.get(0);
        String name;
        if (nameArg == null) {
            name = "";
        } else name = nameArg.toString();
        
        boolean isBuy = (Boolean) args.get(1);
        OrderSide side = (isBuy) ? OrderSide.BUY : OrderSide.SELL;
        
        int quantity = Integer.parseInt(args.get(2).toString());
        if (quantity <= 0) throw new RuntimeException("Negative quantity in CreateOrderFn");

        double limitPrice = Double.NaN;
        double stopPrice = Double.NaN;

        if (args.size() >= 4) {
            Object limitArg = args.get(3);
            if (limitArg != null) {
                limitPrice = Double.parseDouble(limitArg.toString());
                if (limitPrice < 0) throw new IllegalArgumentException("Limit price cannot be negative");
            }
        }

        if (args.size() >= 5 && args.get(4) != null) {
            stopPrice = Double.parseDouble(args.get(4).toString());
            if (stopPrice < 0) throw new IllegalArgumentException("Stop price cannot be negative");
        }

        OrderType orderType = getOrderType(limitPrice, stopPrice);

        return new CreateOrderFnArguments(name, side, quantity, orderType, limitPrice, stopPrice);
    }

    private static OrderType getOrderType(double limitPrice, double stopPrice) {
        OrderType orderType;
        boolean hasLimit = !Double.isNaN(limitPrice);
        boolean hasStop = !Double.isNaN(stopPrice);
        if (!hasLimit && !hasStop) {
            orderType = OrderType.Market;
        } else if (hasLimit && !hasStop) {
            orderType = OrderType.Limit;
            if (limitPrice <= 0) throw new IllegalArgumentException("Limit orders require a positive limit price");
        } else if (!hasLimit) {
            orderType = OrderType.Stop;
            if (stopPrice <= 0) throw new IllegalArgumentException("Stop orders require a positive stop price");
        } else {
            orderType = OrderType.StopLimit;
            if (limitPrice <= 0) throw new IllegalArgumentException("Stop-limit orders require a positive limit price");
            if (stopPrice <= 0) throw new IllegalArgumentException("Stop-limit orders require a positive stop price");
        }
        return orderType;
    }
}

record CreateOrderFnArguments(String name, OrderSide side, int quantity, OrderType orderType, double limitPrice, double stopPrice) {}
