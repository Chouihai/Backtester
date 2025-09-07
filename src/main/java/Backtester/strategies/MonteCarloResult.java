package Backtester.strategies;

import java.util.Map;

public class MonteCarloResult {
    public final int permutations;
    private final Map<String, StatSummary> summaries;
    public final double probLoss; // fraction of paths with net profit < 0
    public final double expectedShortfall5; // ES at 5% on net profit

    // Equity mean curve per time index (others removed)
    public final double[] eqMean;

    public MonteCarloResult(int permutations,
                            Map<String, StatSummary> summaries,
                            double probLoss,
                            double expectedShortfall5,
                            double[] eqMean) {
        this.permutations = permutations;
        this.summaries = summaries;
        this.probLoss = probLoss;
        this.expectedShortfall5 = expectedShortfall5;
        this.eqMean = eqMean;
    }

    public StatSummary getSummary(String key) {
        return summaries.get(key);
    }

    public java.util.Map<String, StatSummary> getSummaries() {
        return java.util.Collections.unmodifiableMap(summaries);
    }
}
