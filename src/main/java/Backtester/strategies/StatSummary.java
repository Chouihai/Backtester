package Backtester.strategies;

public class StatSummary {
    public final double mean;
    public final double median;
    public final double p5;
    public final double p25;
    public final double p75;
    public final double p95;

    public StatSummary(double mean, double median, double p5, double p25, double p75, double p95) {
        this.mean = mean;
        this.median = median;
        this.p5 = p5;
        this.p25 = p25;
        this.p75 = p75;
        this.p95 = p95;
    }
}

