package Backtester.strategies;

import Backtester.objects.Bar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates synthetic price paths using a Brownian bridge on log-closes,
 * preserving each bar's OHLC ratios relative to close.
 */
public class BrownianBridgePricePathGenerator implements PricePathGenerator {
    private final List<Bar> baseBars;

    public BrownianBridgePricePathGenerator(List<Bar> baseBars) {
        this.baseBars = new ArrayList<>(baseBars);
    }

    @Override
    public List<Bar> generate(long seed) {
        int n = baseBars.size();
        if (n == 0) return List.of();

        double c0 = baseBars.get(0).close;
        double cT = baseBars.get(n - 1).close;
        if (c0 <= 0 || cT <= 0) {
            List<Bar> copy = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                Bar b = baseBars.get(i);
                copy.add(new Bar(i, b.date, b.open, b.high, b.low, b.close, b.volume));
            }
            return copy;
        }

        // Estimate sigma from historical log-returns
        List<Double> rets = new ArrayList<>(Math.max(0, n - 1));
        for (int i = 1; i < n; i++) {
            double prev = baseBars.get(i - 1).close;
            double cur = baseBars.get(i).close;
            if (prev > 0 && cur > 0) {
                rets.add(Math.log(cur / prev));
            }
        }
        double mean = 0.0;
        for (double r : rets) mean += r;
        mean = rets.isEmpty() ? 0.0 : mean / rets.size();
        double var = 0.0;
        for (double r : rets) var += Math.pow(r - mean, 2);
        double sigma = (rets.size() > 1) ? Math.sqrt(var / (rets.size() - 1)) : 0.0;
        if (sigma == 0.0) sigma = 1e-6;

        Random rnd = new Random(seed);
        double targetSum = Math.log(cT / c0);
        double sum = 0.0;
        double[] eps = new double[Math.max(0, n - 1)];
        for (int i = 0; i < eps.length; i++) {
            eps[i] = rnd.nextGaussian() * sigma;
            sum += eps[i];
        }
        double adjust = (eps.length == 0) ? 0.0 : (targetSum - sum) / eps.length;

        double[] closes = new double[n];
        closes[0] = c0;
        double acc = 0.0;
        for (int i = 1; i < n; i++) {
            acc += eps[i - 1] + adjust;
            closes[i] = c0 * Math.exp(acc);
        }
        closes[n - 1] = cT; // force exact endpoint

        List<Bar> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Bar src = baseBars.get(i);
            double oc = (src.close != 0) ? src.open / src.close : 1.0;
            double hc = (src.close != 0) ? src.high / src.close : 1.0;
            double lc = (src.close != 0) ? src.low / src.close : 1.0;

            double close = closes[i];
            double open = close * oc;
            double high = close * hc;
            double low = close * lc;
            out.add(new Bar(i, src.date, open, high, low, close, src.volume));
        }
        return out;
    }
}

