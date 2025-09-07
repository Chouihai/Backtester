package Backtester.strategies;

import Backtester.objects.Bar;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Runs Monte Carlo permutations concurrently. The lookback bars remain fixed across permutations.
 * Each task generates a Brownian-bridge-like permuted copy of the base bars, concatenates the
 * lookback bars, then executes a StrategyRunner and aggregates average metrics across permutations.
 */
public class MonteCarloRunner {

    private final List<Bar> lookbackBars;
    private final List<Bar> baseBars;
    private final String script;
    private final Logger logger;
    private final PricePathGenerator pathGenerator;

    public MonteCarloRunner(List<Bar> lookbackBars,
                            List<Bar> baseBars,
                            String script,
                            Logger logger) {
        this.lookbackBars = new ArrayList<>(lookbackBars);
        this.baseBars = new ArrayList<>(baseBars);
        this.script = script;
        this.logger = logger;
        this.pathGenerator = new BrownianBridgePricePathGenerator(this.baseBars);
    }

    public MonteCarloRunner(List<Bar> lookbackBars,
                            List<Bar> baseBars,
                            String script,
                            Logger logger,
                            PricePathGenerator generator) {
        this.lookbackBars = new ArrayList<>(lookbackBars);
        this.baseBars = new ArrayList<>(baseBars);
        this.script = script;
        this.logger = logger;
        this.pathGenerator = generator;
    }

    public MonteCarloResult run(int permutations,
                                int threads,
                                double initialCapital) {
        if (permutations <= 0) {
            return new MonteCarloResult(0, java.util.Collections.emptyMap(), 0.0, 0.0,
                    new double[0], new double[0], new double[0], new double[0], new double[0], new double[0]);
        }

        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, threads));
        try {
            List<Future<Outcome>> futures = new ArrayList<>(permutations);
            Random seedGen = new Random();
            for (int i = 0; i < permutations; i++) {
                long seed = seedGen.nextLong();
                futures.add(pool.submit(task(seed, initialCapital)));
            }

            List<Double> netProfits = new ArrayList<>(permutations);
            List<Double> totalPnls = new ArrayList<>(permutations); // closed + open
            List<Double> drawdowns = new ArrayList<>(permutations);
            List<Double> sharpes = new ArrayList<>(permutations);
            List<Double> tradesCounts = new ArrayList<>(permutations);
            List<Double> cagrs = new ArrayList<>(permutations);
            List<Double> calmars = new ArrayList<>(permutations);
            List<Double> sortinos = new ArrayList<>(permutations);
            List<Double> vols = new ArrayList<>(permutations);
            List<double[]> equityCurves = new ArrayList<>(permutations);

            for (Future<Outcome> f : futures) {
                Outcome oc = f.get();
                RunResult rr = oc.result;
                equityCurves.add(toPrimitive(rr.strategyEquity()));

                double net = rr.netProfit();
                netProfits.add(net);
                double totalPnl = net + rr.openPnL();
                totalPnls.add(totalPnl);
                drawdowns.add(rr.maxDrawdown());
                sharpes.add(rr.sharpe());

                int entries = rr.trades().size();
                tradesCounts.add((double) entries);

                cagrs.add(rr.cagr());
                calmars.add(rr.calmar());
                vols.add(rr.volatility());
                sortinos.add(rr.sortino());
            }

            java.util.Map<String, StatSummary> summaries = new java.util.LinkedHashMap<>();
            summaries.put("Net Profit", summarize(netProfits));
            summaries.put("Max Drawdown", summarize(drawdowns));
            summaries.put("Total PnL (Realized+Unrealized)", summarize(totalPnls));
            summaries.put("Sharpe", summarize(sharpes));
            summaries.put("Trades", summarize(tradesCounts));
            summaries.put("CAGR", summarize(cagrs));
            summaries.put("Calmar", summarize(calmars));
            summaries.put("Volatility", summarize(vols));
            summaries.put("Sortino", summarize(sortinos));

            int p = permutations;
            double probLoss = (double) netProfits.stream().filter(v -> v < 0).count() / p;
            double es5 = expectedShortfall(netProfits, 0.05);

            double[][] bands = computeEquityBands(equityCurves);
            return new MonteCarloResult(p, summaries, probLoss, es5,
                    bands[0], bands[1], bands[2], bands[3], bands[4], bands[5]);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Monte Carlo run interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Monte Carlo task failed", e.getCause());
        } finally {
            pool.shutdownNow();
        }
    }

    private Callable<Outcome> task(long seed, double initialCapital) {
        return () -> {
            List<Bar> permutedBase = pathGenerator.generate(seed);
            StrategyRunner runner = new StrategyRunner(permutedBase, lookbackBars, script, logger);
            RunResult res = runner.run(initialCapital);
            return new Outcome(res, permutedBase);
        };
    }

    private static class Outcome {
        final RunResult result;
        final List<Bar> path;

        Outcome(RunResult result, List<Bar> path) {
            this.result = result;
            this.path = path;
        }
    }

    private static StatSummary summarize(List<Double> values) {
        if (values.isEmpty()) return new StatSummary(0, 0, 0, 0, 0, 0);
        List<Double> sorted = new java.util.ArrayList<>(values);
        Collections.sort(sorted);
        double mean = sorted.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double median = percentile(sorted, 0.5);
        double p5 = percentile(sorted, 0.05);
        double p25 = percentile(sorted, 0.25);
        double p75 = percentile(sorted, 0.75);
        double p95 = percentile(sorted, 0.95);
        return new StatSummary(mean, median, p5, p25, p75, p95);
    }

    private static double[] toPrimitive(List<Double> list) {
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    // Returns {mean, p5, p25, p50, p75, p95} as arrays over time
    private static double[][] computeEquityBands(List<double[]> curves) {
        if (curves.isEmpty()) return new double[][]{new double[0],new double[0],new double[0],new double[0],new double[0],new double[0]};
        int n = curves.get(0).length;
        double[] mean = new double[n];
        double[] p5 = new double[n];
        double[] p25 = new double[n];
        double[] p50 = new double[n];
        double[] p75 = new double[n];
        double[] p95 = new double[n];
        double[] col = new double[curves.size()];
        for (int t = 0; t < n; t++) {
            double sum = 0.0;
            for (int i = 0; i < curves.size(); i++) {
                double v = curves.get(i)[t];
                col[i] = v;
                sum += v;
            }
            mean[t] = sum / curves.size();
            java.util.Arrays.sort(col);
            p5[t] = interp(col, 0.05);
            p25[t] = interp(col, 0.25);
            p50[t] = interp(col, 0.50);
            p75[t] = interp(col, 0.75);
            p95[t] = interp(col, 0.95);
        }
        return new double[][]{mean, p5, p25, p50, p75, p95};
    }

    private static double interp(double[] sorted, double q) {
        if (sorted.length == 0) return 0.0;
        double pos = q * (sorted.length - 1);
        int i = (int) Math.floor(pos);
        int j = Math.min(sorted.length - 1, i + 1);
        double w = pos - i;
        return sorted[i] * (1 - w) + sorted[j] * w;
    }

    private static double percentile(List<Double> sortedAsc, double q) {
        if (sortedAsc.isEmpty()) return 0.0;
        double pos = q * (sortedAsc.size() - 1);
        int i = (int) Math.floor(pos);
        int j = Math.min(sortedAsc.size() - 1, i + 1);
        double w = pos - i;
        return sortedAsc.get(i) * (1 - w) + sortedAsc.get(j) * w;
    }

    private static double expectedShortfall(java.util.List<Double> values, double alpha) {
        if (values.isEmpty()) return 0.0;
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int cutoff = Math.max(1, (int) Math.floor(alpha * sorted.size()));
        double sum = 0.0;
        for (int i = 0; i < cutoff; i++) sum += sorted.get(i);
        return sum / cutoff;
    }
}
