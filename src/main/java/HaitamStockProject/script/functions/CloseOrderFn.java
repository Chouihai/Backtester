package HaitamStockProject.script.functions;

import HaitamStockProject.backtester.caches.OrderCache;
import HaitamStockProject.script.EvaluationContext;
import HaitamStockProject.script.functions.result.ScriptFunctionResult;

import java.util.List;

public class CloseOrderFn implements ScriptFunction {

    private final static int ARGUMENTS_SIZE = 3; // Later on might want to have required vs optional args
    private final OrderCache orderCache;
//    private final BacktestRunPositionCache positionCache;


    public CloseOrderFn(OrderCache orderCache) {
        this.orderCache = orderCache;
    }

    public ScriptFunctionResult execute(List<Object> args, EvaluationContext context) {
        // TODO: implement this later
//        CloseOrderFnArguments arguments = validateArgs(args);
//        Set<Order> orders = orderCache.getOrder(arguments.id());
//        for (Order order : orders) {
//            switch (order.status()) {
//                case FILLED:
//                    // Only filled orders require a real offsetting close
//                    Order closeOrder = new Order(arguments.id(), order.symbol(), OrderSide.flip(order.side()), );
////                    closeOrder.setId(id + "_close_" + order.getInternalId());
////                    closeOrder.setSide(oppositeSide(order.getSide()));
////                    closeOrder.setQty(order.getQty());
////                    closeOrder.setStatus(OrderStatus.NEW);
////                    closeOrder.setBarSubmitted(currentBar);
////                    submitOrder(closeOrder);
//                    break;
//
//                case SUBMITTED:
//                    // Pending orders should be cancelled directly
//                    order.setStatus(OrderStatus.CANCELLED);
//                    break;
//
//                default:
//                    // Do nothing for already cancelled, expired, or closed orders
//                    break;
//            }
//        }
        return null;
    }

    private CloseOrderFnArguments validateArgs(List<Object> args) {
        if (args.size() != ARGUMENTS_SIZE) throw new IllegalArgumentException("Bad arguments size in EnterPosition");
        String id = args.getFirst().toString();
        return new CloseOrderFnArguments(id);
    }
}

record CloseOrderFnArguments(String id) {}
