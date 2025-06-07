package HaitamStockProject.strategies;

import HaitamStockProject.objects.Order;

import java.util.List;

public interface Strategy<T> {

    List<Order> roll(T inputs);
}

// The evaluator will simply evaluate the text, it will not take any function definitions
// The responsibility for passing in the function definitions will be laid upon whoever is injecting the evaluator.
// Inside of Backtester, we will pass in a bar to the strategy runner.
// We update the context of the expression with the new data.
// We run the expression with the definitions given to the functions inside that expression.