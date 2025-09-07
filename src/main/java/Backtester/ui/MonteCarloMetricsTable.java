package Backtester.ui;

import Backtester.strategies.MonteCarloResult;
import Backtester.strategies.StatSummary;
import javafx.collections.FXCollections;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

public class MonteCarloMetricsTable {
    private final TableView<MonteCarloMetricRow> table;

    public MonteCarloMetricsTable(TableView<MonteCarloMetricRow> table) {
        this.table = table;
    }

    public void populate(MonteCarloResult mc) {
        if (table == null) return;
        List<MonteCarloMetricRow> rows = new ArrayList<>();
        add(rows, "Net Profit (Realized)", mc.getSummary("Net Profit"), true, false);
        add(rows, "Total PnL (Realized+Unrealized)", mc.getSummary("Total PnL (Realized+Unrealized)"), true, false);
        add(rows, "Max Drawdown", mc.getSummary("Max Drawdown"), false, true);
        add(rows, "Sharpe", mc.getSummary("Sharpe"), false, false);
        add(rows, "Sortino", mc.getSummary("Sortino"), false, false);
        add(rows, "Volatility", mc.getSummary("Volatility"), false, true);
        add(rows, "Trades", mc.getSummary("Trades"), false, false);
        add(rows, "CAGR", mc.getSummary("CAGR"), false, true);
        add(rows, "Calmar", mc.getSummary("Calmar"), false, false);

        rows.add(new MonteCarloMetricRow("Prob. Loss", UiFormat.formatPercentage(mc.probLoss), "-", "-", "-", "-", "-"));
        rows.add(new MonteCarloMetricRow("ES (5%) Net", UiFormat.formatCurrency(mc.expectedShortfall5), "-", "-", "-", "-", "-"));

        table.setItems(FXCollections.observableArrayList(rows));
    }

    private void add(List<MonteCarloMetricRow> rows, String name, StatSummary s, boolean currency, boolean percent) {
        if (s == null) return;
        String mean = currency ? UiFormat.formatCurrency(s.mean) : percent ? UiFormat.formatPercentage(s.mean) : UiFormat.formatDecimal(s.mean);
        String median = currency ? UiFormat.formatCurrency(s.median) : percent ? UiFormat.formatPercentage(s.median) : UiFormat.formatDecimal(s.median);
        String p5 = currency ? UiFormat.formatCurrency(s.p5) : percent ? UiFormat.formatPercentage(s.p5) : UiFormat.formatDecimal(s.p5);
        String p25 = currency ? UiFormat.formatCurrency(s.p25) : percent ? UiFormat.formatPercentage(s.p25) : UiFormat.formatDecimal(s.p25);
        String p75 = currency ? UiFormat.formatCurrency(s.p75) : percent ? UiFormat.formatPercentage(s.p75) : UiFormat.formatDecimal(s.p75);
        String p95 = currency ? UiFormat.formatCurrency(s.p95) : percent ? UiFormat.formatPercentage(s.p95) : UiFormat.formatDecimal(s.p95);
        rows.add(new MonteCarloMetricRow(name, mean, median, p5, p25, p75, p95));
    }
}

