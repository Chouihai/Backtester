package Backtester.strategies;

import java.util.Map;

public class MonteCarloResult {
    public final int permutations;
    private final Map<String, StatSummary> summaries;
    public final double probLoss; // fraction of paths with net profit < 0
    public final double expectedShortfall5; // ES at 5% on net profit

    // Equity percentile curves per time index
    public final double[] eqMean;
    public final double[] eqP5;
    public final double[] eqP25;
    public final double[] eqP50;
    public final double[] eqP75;
    public final double[] eqP95;

    public MonteCarloResult(int permutations,
                            Map<String, StatSummary> summaries,
                            double probLoss,
                            double expectedShortfall5,
                            double[] eqMean,
                            double[] eqP5,
                            double[] eqP25,
                            double[] eqP50,
                            double[] eqP75,
                            double[] eqP95) {
        this.permutations = permutations;
        this.summaries = summaries;
        this.probLoss = probLoss;
        this.expectedShortfall5 = expectedShortfall5;
        this.eqMean = eqMean;
        this.eqP5 = eqP5;
        this.eqP25 = eqP25;
        this.eqP50 = eqP50;
        this.eqP75 = eqP75;
        this.eqP95 = eqP95;
    }

    public StatSummary getSummary(String key) {
        return summaries.get(key);
    }

    public java.util.Map<String, StatSummary> getSummaries() {
        return java.util.Collections.unmodifiableMap(summaries);
    }
}
