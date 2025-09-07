package Backtester.strategies;

import Backtester.objects.Bar;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
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
                    new double[0]);
        }

        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, threads));
        try {
            List<Future<Outcome>> futures = new ArrayList<>(permutations);
            Random seedGen = new Random();
            for (int i = 0; i < permutations; i++) {
                long seed = seedGen.nextLong();
                futures.add(pool.submit(task(seed, initialCapital)));
            }

            double[] netProfits = new double[permutations];
            double[] totalPnls = new double[permutations]; // closed + open
            double[] drawdowns = new double[permutations];
            double[] sharpes = new double[permutations];
            double[] tradesCounts = new double[permutations];
            double[] cagrs = new double[permutations];
            double[] calmars = new double[permutations];
            double[] sortinos = new double[permutations];
            double[] vols = new double[permutations];
            // Running mean for equity across permutations
            double[] sumEq = null;

            int idx = 0;
            for (Future<Outcome> f : futures) {
                Outcome oc = f.get();
                RunResult rr = oc.result;
                double[] eq = rr.strategyEquity();
                if (sumEq == null) {
                    sumEq = new double[eq.length];
                }
                int len = Math.min(sumEq.length, eq.length);
                for (int i = 0; i < len; i++) sumEq[i] += eq[i];

                double net = rr.netProfit();
                netProfits[idx] = net;
                totalPnls[idx] = net + rr.openPnL();
                drawdowns[idx] = rr.maxDrawdown();
                sharpes[idx] = rr.sharpe();

                tradesCounts[idx] = rr.trades().size();

                cagrs[idx] = rr.cagr();
                calmars[idx] = rr.calmar();
                vols[idx] = rr.volatility();
                sortinos[idx] = rr.sortino();
                idx++;
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
            int losses = 0; for (double v : netProfits) if (v < 0) losses++; double probLoss = (double) losses / p;
            double es5 = expectedShortfall(netProfits, 0.05);

            double[] meanEq;
            if (sumEq == null) {
                meanEq = new double[0];
            } else {
                meanEq = new double[sumEq.length];
                for (int i = 0; i < sumEq.length; i++) meanEq[i] = sumEq[i] / p;
            }
            return new MonteCarloResult(p, summaries, probLoss, es5, meanEq);
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

    private static StatSummary summarize(double[] values) {
        if (values.length == 0) return new StatSummary(0, 0, 0, 0, 0, 0);
        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        double sum = 0.0;
        for (double v : sorted) sum += v;
        double mean = sum / sorted.length;
        double median = percentile(sorted, 0.5);
        double p5 = percentile(sorted, 0.05);
        double p25 = percentile(sorted, 0.25);
        double p75 = percentile(sorted, 0.75);
        double p95 = percentile(sorted, 0.95);
        return new StatSummary(mean, median, p5, p25, p75, p95);
    }

    private static double interp(double[] sorted, double q) {
        if (sorted.length == 0) return 0.0;
        double pos = q * (sorted.length - 1);
        int i = (int) Math.floor(pos);
        int j = Math.min(sorted.length - 1, i + 1);
        double w = pos - i;
        return sorted[i] * (1 - w) + sorted[j] * w;
    }

    private static double percentile(double[] sortedAsc, double q) {
        if (sortedAsc.length == 0) return 0.0;
        double pos = q * (sortedAsc.length - 1);
        int i = (int) Math.floor(pos);
        int j = Math.min(sortedAsc.length - 1, i + 1);
        double w = pos - i;
        return sortedAsc[i] * (1 - w) + sortedAsc[j] * w;
    }

    private static double expectedShortfall(double[] values, double alpha) {
        if (values.length == 0) return 0.0;
        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        int cutoff = Math.max(1, (int) Math.floor(alpha * sorted.length));
        double sum = 0.0;
        for (int i = 0; i < cutoff; i++) sum += sorted[i];
        return sum / cutoff;
    }
}






