package HaitamStockProject.backtester;

import HaitamStockProject.objects.Order;
import HaitamStockProject.objects.Position;
import HaitamStockProject.objects.SecurityDayValues;
import HaitamStockProject.services.BusinessDayService;
import HaitamStockProject.services.SecurityDataService;
import HaitamStockProject.strategies.Strategy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BackTester {

    private LocalDate currentDate;
    private final LocalDate endDate;
    private final Strategy<SecurityDayValues> strategy;
    private final BusinessDayService businessDayService;
    private final HashMap<LocalDate, SecurityDayValues> securityDayValuesByDate;
    private Double balance;
    private Position position; // Only one position as I support running a backtest on only one stock at the moment
    private List<Order> openOrders;
    private final List<Order> allOrders = new ArrayList<>();

    public BackTester(Double initialBalance,
                      LocalDate startDate,
                      LocalDate endDate,
                      Strategy<SecurityDayValues> strategy,
                      String ticker,
                      SecurityDataService securityData,
                      BusinessDayService businessDayService) {
        this.currentDate = startDate;
        this.endDate = endDate;
        this.strategy = strategy;
        this.securityDayValuesByDate = securityData.fetchSecurityDayValues(ticker, startDate, endDate);
        this.balance = initialBalance;
        this.businessDayService = businessDayService;
        this.position = new Position(ticker, 0);
    }

    // TODO: incomplete
    public BacktesterResult run() {
        while (currentDate.isBefore(endDate)) {
            // First we check if we need to make any trades
            List<Order> orders = strategy.roll(securityDayValuesByDate.get(currentDate));
            if (!orders.isEmpty()) {
                allOrders.addAll(orders);
                this.openOrders.addAll(orders);
            }

            executeTrades();
            this.currentDate = businessDayService.nextBusinessDay(currentDate);
        }
        return null;
    }

    // TODO: Perhaps Implement a tradeExecutor. It takes positions and orders to execute, and returns updated positions and closedOrders.
    private void executeTrades() {
        double openPrice = securityDayValuesByDate.get(currentDate).getOpen();
        for (Order order: this.openOrders) {
            if (order.getSettleDate() == currentDate) {
                if (order.getSignedQuantity() < 0) {
                    int sharesSold = Math.abs((position.getQuantity() + order.getSignedQuantity() > 0) ? order.getSignedQuantity() : position.getQuantity());
                    position.setQuantity(position.getQuantity() - sharesSold);
                    balance += sharesSold * openPrice;
                } else if (order.getSignedQuantity() > 0){
                    balance -= order.getSignedQuantity() * openPrice;
                    position.addShares(order.getSignedQuantity());
                }
                this.openOrders.remove(order); // TODO: might need to be more efficient (Probably make this a queue)
            }
        }
    }
}
