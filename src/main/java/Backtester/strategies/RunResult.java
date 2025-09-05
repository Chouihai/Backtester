package Backtester.strategies;

import Backtester.objects.Bar;
import Backtester.objects.Trade;

import java.util.List;

public record RunResult(List<Trade> trades,
                        double netProfit,
                        double grossProfit,
                        double grossLoss,
                        double sharpe,
                        double openPnL,
                        double maxDrawdown,
                        double maxRunup,
                        Bar lastBar,
                        List<Double> strategyEquity) {
}
