package Backtester.strategies;

public class MonteCarloResult {
    public final int permutations;
    public final double avgNetProfit;
    public final double avgMaxDrawdown;
    public final double avgSharpe;
    public final double avgEntries;
    public final double avgOpenTrades;
    public final double avgClosedTrades;
    public final double avgWinners;
    public final double avgLosers;

    public MonteCarloResult(int permutations,
                            double avgNetProfit,
                            double avgMaxDrawdown,
                            double avgSharpe,
                            double avgEntries,
                            double avgOpenTrades,
                            double avgClosedTrades,
                            double avgWinners,
                            double avgLosers) {
        this.permutations = permutations;
        this.avgNetProfit = avgNetProfit;
        this.avgMaxDrawdown = avgMaxDrawdown;
        this.avgSharpe = avgSharpe;
        this.avgEntries = avgEntries;
        this.avgOpenTrades = avgOpenTrades;
        this.avgClosedTrades = avgClosedTrades;
        this.avgWinners = avgWinners;
        this.avgLosers = avgLosers;
    }
}

