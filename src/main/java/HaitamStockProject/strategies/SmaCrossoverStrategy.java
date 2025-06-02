package HaitamStockProject.strategies;

import HaitamStockProject.objects.Order;
import HaitamStockProject.objects.SecurityDayValues;
import HaitamStockProject.services.BusinessDayService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SmaCrossoverStrategy implements Strategy<SecurityDayValues> {
    private final String symbol;
    private final int quantity;
    private final SmaCalculator smaA;
    private final SmaCalculator smaB;
    private int previousSign = 0;
    private double mostRecentOpen;
    private LocalDate currentDate;
    private final BusinessDayService businessDayService;

    public SmaCrossoverStrategy(int numDaysA, int numDaysB, int quantity, String symbol, double mostRecentOpen, LocalDate date, List<Double> closePrices, BusinessDayService businessDayService) {
        this.quantity = quantity;
        this.symbol = symbol;
        this.businessDayService = businessDayService;
        this.mostRecentOpen = mostRecentOpen;
        this.currentDate = date;

        smaA = new SmaCalculator(numDaysA, closePrices);
        smaB = new SmaCalculator(numDaysB, closePrices);
    }


    public List<Order> roll(SecurityDayValues inputs) {
        updateInputs(inputs);
        return makeTrades();
    }

    /**
     * Checks for buy/sell signals and creates an order
     */
    private List<Order> makeTrades() {
        List<Order> result = new ArrayList<>();
        if (didCrossingHappen()) {
            if (smaA.getAverage() > smaB.getAverage()) {
                result.add(new Order(symbol, mostRecentOpen, quantity, currentDate, businessDayService.nextBusinessDay(currentDate)));
            } else {
                result.add(new Order(symbol, mostRecentOpen, -1 * quantity, currentDate, businessDayService.nextBusinessDay(currentDate)));
            }
            return result;
        } else return result;
    }

    private boolean didCrossingHappen() {
        double diff = smaA.getAverage() - smaB.getAverage();
        int curr_sign = 0;
        if (diff > 0) {
            curr_sign = 1;
        } else  if (diff < 0) {
            curr_sign = -1;
        }
        return previousSign != 0 && curr_sign != 0 && curr_sign != previousSign;
    }

    /**
     * Updates the variables and the date
     */
    private void updateInputs(SecurityDayValues inputs) {
        double diff = smaA.getAverage() - smaB.getAverage();
        if (diff > 0) {
            previousSign = 1;
        } else  if (diff < 0) {
            previousSign = -1;
        }
        smaA.addValue(inputs.getClose());
        smaB.addValue(inputs.getClose());
        currentDate = businessDayService.nextBusinessDay(currentDate);
        this.mostRecentOpen = inputs.getOpen();
    }
}
