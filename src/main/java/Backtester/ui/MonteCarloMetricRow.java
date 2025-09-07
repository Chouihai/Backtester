package Backtester.ui;

public class MonteCarloMetricRow {
    private final String metric;
    private final String mean;
    private final String median;
    private final String p5;
    private final String p25;
    private final String p75;
    private final String p95;

    public MonteCarloMetricRow(String metric, String mean, String median, String p5, String p25, String p75, String p95) {
        this.metric = metric;
        this.mean = mean;
        this.median = median;
        this.p5 = p5;
        this.p25 = p25;
        this.p75 = p75;
        this.p95 = p95;
    }

    public String getMetric() { return metric; }
    public String getMean() { return mean; }
    public String getMedian() { return median; }
    public String getP5() { return p5; }
    public String getP25() { return p25; }
    public String getP75() { return p75; }
    public String getP95() { return p95; }
}

