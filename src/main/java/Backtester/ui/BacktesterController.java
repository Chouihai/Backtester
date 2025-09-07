package Backtester.ui;

import Backtester.objects.Bar;
import Backtester.objects.Trade;
import Backtester.script.tokens.Parser;
import Backtester.services.HistoricalDataService;
import Backtester.strategies.MonteCarloResult;
import Backtester.strategies.MonteCarloRunner;
import Backtester.strategies.RunResult;
import Backtester.strategies.StrategyRunner;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BacktesterController {

    private static final Logger logger = LoggerFactory.getLogger(BacktesterController.class);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Services
    private final HistoricalDataService historicalDataService;

    // UI Components
    public TextField symbolField;
    public DatePicker startDatePicker;
    public DatePicker endDatePicker;
    public TextField initialCapitalField;
    public TextArea strategyTextArea;
    public Button runButton;
    public Button stopButton;
    public VBox chartContainer;
    public TableView<Trade> tradesTable;
    public Label netProfitLabel;
    public Label grossProfitLabel;
    public Label grossLossLabel;
    public Label openPnLLabel;
    public Label maxDrawdownLabel;
    public Label maxRunUpLabel;
    public Label sharpeRatioLabel;
    public Label tradesMadeLabel;
    public Label winningTradesLabel;
    public ProgressBar progressBar;
    public Label statusLabel;
    public TextField permutationsField;

    // Monte Carlo output labels
    public Label mcTitleLabel;
    public TableView<MonteCarloMetricRow> mcMetricsTable;
    public VBox mcPathsContainer;
    public Bar lastBar;
    private List<LocalDate> lastDates;

    private ObservableList<Trade> tradesData = FXCollections.observableArrayList();
    private volatile boolean isRunning = false;

    public BacktesterController(HistoricalDataService historicalDataService) {
        this.historicalDataService = historicalDataService;
    }

    @FXML
    public void initialize() {
        setupUI();
        setupEventHandlers();
    }

    private void setupUI() {
        // Default inputs
        startDatePicker.setValue(LocalDate.of(2024, 3, 14));
        endDatePicker.setValue(LocalDate.of(2025, 5, 1));
        if (symbolField != null) {
            symbolField.setText("AAPL");
        }
        if (strategyTextArea != null) {
            String defaultStrategy = """
                    sma20 = sma(20)
                    sma50 = sma(50)

                    if (crossover(sma20, sma50)):
                        createOrder("long", true, 1000)
                    if (crossover(sma50, sma20)):
                        createOrder("position1", false, 1000)
                    """;
            strategyTextArea.setText(defaultStrategy);
        }
        initialCapitalField.setText("100000");
        if (permutationsField != null) permutationsField.setText("100");
        tradesTable.setItems(tradesData);
        progressBar.setVisible(false);
        statusLabel.setText("Ready to run backtest");
        stopButton.setDisable(true);
    }

    private void setupEventHandlers() {
        runButton.setOnAction(e -> runBacktest());
        stopButton.setOnAction(e -> stopBacktest());
    }

    @FXML
    private void runBacktest() {
        if (isRunning) {
            showAlert("Backtest already running", "Please wait for the current backtest to complete.");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                runBacktestAsync();
            } catch (Exception e) {
                logger.error("Backtest failed", e);
                Platform.runLater(() -> {
                    showAlert("Backtest Failed", "Error: " + e.getMessage());
                    resetUI();
                });
            }
        }, executorService);
    }

    private void runBacktestAsync() {
        Platform.runLater(() -> {
            isRunning = true;
            runButton.setDisable(true);
            stopButton.setDisable(false);
            progressBar.setVisible(true);
            statusLabel.setText("Fetching historical data...");
        });

        try {
            String symbol = symbolField.getText().trim().toUpperCase();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            double initialCapital = Double.parseDouble(initialCapitalField.getText()); // Nothing will be done with initial capital for now
            String strategyScript = strategyTextArea.getText();

            Platform.runLater(() -> statusLabel.setText("Fetching data for " + symbol + "..."));
            List<Bar> lookbackBars = historicalDataService.getHistoricalData(symbol, startDate.minusDays(100), startDate);
            List<Bar> bars = historicalDataService.getHistoricalData(symbol, startDate, endDate);
            logger.info("Fetched {} bars for {} within selected range.", bars.size(), symbol);

            if (bars.isEmpty()) {
                throw new RuntimeException("No data available for " + symbol + ". Please check the symbol and try again.");
            }

            final List<LocalDate> dates = new ArrayList<>();
            final List<Double> buyAndHoldEquity = new ArrayList<>();
            double shares = initialCapital / bars.getFirst().close;

            for (Bar bar: bars){
                dates.add(bar.date);
                buyAndHoldEquity.add(shares * bar.close);
            }

            Platform.runLater(() -> statusLabel.setText("Running strategy..."));

            RunResult results = null;
            if (!strategyScript.trim().isEmpty()) {
                try {
                    StrategyRunner strategyRunner = new StrategyRunner(bars, lookbackBars, strategyScript, logger);
                    results = strategyRunner.run(initialCapital);
                } catch (Exception e) {
                    throw new RuntimeException("Strategy parsing failed: " + e.getMessage());
                }
            }

            // Update UI with backtest results
            RunResult finalResults = results;
            Platform.runLater(() -> {
                if (finalResults != null) {
                    updateResults(finalResults, dates, buyAndHoldEquity);
                    statusLabel.setText("Backtest completed. Preparing Monte Carlo...");
                }
            });

            // Run Monte Carlo permutations (if requested)
            int permutations = parseOrDefault(permutationsField != null ? permutationsField.getText() : "0", 0);
            if (permutations > 0) {
                Platform.runLater(() -> statusLabel.setText("Running Monte Carlo (" + permutations + ")..."));
                MonteCarloRunner mcRunner = new MonteCarloRunner(lookbackBars, bars, strategyScript, logger);
                MonteCarloResult mc = mcRunner.run(permutations, Math.min(4, permutations), initialCapital);
                Platform.runLater(() -> {
                    if (mcTitleLabel != null) {
                        mcTitleLabel.setText("Monte Carlo Metrics");
                    }
                    updateMonteCarloResults(mc);
                    updateMonteCarloPaths(mc);
                    statusLabel.setText("Monte Carlo completed");
                    resetUI();
                });
            } else {
                Platform.runLater(() -> {
                    statusLabel.setText("Backtest completed");
                    resetUI();
                });
            }
        } catch (Exception e) {
            logger.error("Backtest failed", e);
            Platform.runLater(() -> {
                showAlert("Backtest Failed", "Error: " + e.getMessage());
                resetUI();
            });
        }
    }

    private boolean validateInputs() {
        if (symbolField.getText().trim().isEmpty()) {
            showAlert("Invalid Input", "Please enter a stock symbol.");
            return false;
        }

        if (strategyTextArea.getText().trim().isEmpty()) {
            showAlert("Invalid Input", "Cannot have an empty strategy");
            return false;
        }

        try {
            Parser parser = new Parser();
            String s = strategyTextArea.getText().trim();
            parser.parse(strategyTextArea.getText().trim());
        } catch (Exception e) {
            logger.error(e.getMessage());
            showAlert("Invalid Input", "Could not parse strategy. Error: " + e);
            return false;
        }

        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showAlert("Invalid Input", "Please select both start and end dates.");
            return false;
        }

        if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
            showAlert("Invalid Input", "Start date must be before end date.");
            return false;
        }

        try {
            double capital = Double.parseDouble(initialCapitalField.getText());
            if (capital <= 0) {
                showAlert("Invalid Input", "Initial capital must be greater than 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid initial capital amount.");
            return false;
        }

        return true;
    }

    private void updateResults(RunResult results, List<LocalDate> dates, List<Double> buyAndHold) {
        tradesData.clear();
        tradesData.addAll(results.trades());

        double netProfit = results.netProfit();
        double grossProfit = results.grossProfit();
        double grossLoss = results.grossLoss();
        double openPnL = results.openPnL();
        double maxDrawdown = results.maxDrawdown();
        double maxRunUp = results.maxRunup();
        double sharpeRatio = results.sharpe();
        lastBar = results.lastBar();

        int tradesMade = results.trades().size();
        long winningTrades = results.trades().stream().filter(t -> t.isClosed() && t.profit() > 0).count();

        netProfitLabel.setText(formatCurrency(netProfit));
        setLabelColor(netProfitLabel, netProfit);
        
        grossProfitLabel.setText(formatCurrency(grossProfit));
        grossProfitLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #27ae60;"); // Always green
        
        grossLossLabel.setText(formatCurrency(grossLoss));
        grossLossLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e74c3c;"); // Always red
        
        openPnLLabel.setText(formatCurrency(openPnL));
        setLabelColor(openPnLLabel, openPnL);
        
        maxDrawdownLabel.setText(formatPercentage(maxDrawdown));
        setLabelColor(maxDrawdownLabel, maxDrawdown);
        
        maxRunUpLabel.setText(formatPercentage(maxRunUp));
        setLabelColor(maxRunUpLabel, maxRunUp);
        
        sharpeRatioLabel.setText(formatDecimal(sharpeRatio));
        setLabelColor(sharpeRatioLabel, sharpeRatio);

        if (tradesMadeLabel != null) tradesMadeLabel.setText(String.valueOf(tradesMade));
        if (winningTradesLabel != null) winningTradesLabel.setText(String.valueOf(winningTrades));

        this.lastDates = dates;
        updateChart(results, dates, buyAndHold);
    }

    private String formatCurrency(double value) {
        return String.format("$%,.2f", value);
    }

    private String formatPercentage(double value) {
        if (Double.isNaN(value)) {
            return "0.00%";
        }
        return String.format("%.2f%%", value * 100);
    }

    private String formatDecimal(double value) {
        if (Double.isNaN(value)) {
            return "0.00";
        }
        return String.format("%.2f", value);
    }

    private int parseOrDefault(String text, int def) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private void updateChart(RunResult result, List<LocalDate> dates, List<Double> buyAndHold) {
        if (chartContainer == null) return;
        chartContainer.getChildren().clear();

        var strat = result.strategyEquity();

        if (dates.isEmpty() || strat.isEmpty() || buyAndHold.isEmpty()) {
            Label chartLabel = new Label("No equity data. Run a backtest.");
            chartLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            chartContainer.getChildren().add(chartLabel);
            return;
        }

        TimeSeries strategySeries = new TimeSeries("Strategy");
        TimeSeries bhSeries = new TimeSeries("Buy & Hold");
        for (int i = 0; i < dates.size(); i++) {
            LocalDate d = dates.get(i);
            Day day = new Day(d.getDayOfMonth(), d.getMonthValue(), d.getYear());
            strategySeries.add(day, strat.get(i));
            bhSeries.add(day, buyAndHold.get(i));
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(strategySeries);
        dataset.addSeries(bhSeries);

        // No internal chart title; the section already has a title
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,
                "Date",
                "Equity ($)",
                dataset,
                true,
                true,
                false
        );

        // Some basic styling
        XYPlot plot = chart.getXYPlot();
        XYItemRenderer renderer = plot.getRenderer();
        if (renderer instanceof XYLineAndShapeRenderer lineRenderer) {
            lineRenderer.setDefaultShapesVisible(false);
            lineRenderer.setSeriesPaint(0, new Color(39, 174, 96));
            lineRenderer.setSeriesPaint(1, new Color(52, 152, 219));
            lineRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
            lineRenderer.setSeriesStroke(1, new BasicStroke(2.0f));
        }

        // Interactive viewer (zoom/pan)
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        ChartViewer viewer = new ChartViewer(chart);
        viewer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        viewer.prefWidthProperty().bind(chartContainer.widthProperty());
        viewer.prefHeightProperty().bind(chartContainer.heightProperty());
        VBox.setVgrow(viewer, Priority.ALWAYS);
        chartContainer.getChildren().setAll(viewer);
    }

    private void updateMonteCarloResults(MonteCarloResult mc) {
        if (mcMetricsTable == null) return;
        try {
            List<MonteCarloMetricRow> rows = new ArrayList<>();

            addMetricRow(rows, "Net Profit (Realized)", mc.getSummary("Net Profit"), true, false);
            addMetricRow(rows, "Total PnL (Realized+Unrealized)", mc.getSummary("Total PnL (Realized+Unrealized)"), true, false);
            addMetricRow(rows, "Max Drawdown", mc.getSummary("Max Drawdown"), false, true);
            addMetricRow(rows, "Sharpe", mc.getSummary("Sharpe"), false, false);
            addMetricRow(rows, "Sortino", mc.getSummary("Sortino"), false, false);
            addMetricRow(rows, "Volatility", mc.getSummary("Volatility"), false, true);
            addMetricRow(rows, "Trades", mc.getSummary("Trades"), false, false);
            addMetricRow(rows, "CAGR", mc.getSummary("CAGR"), false, true);
            addMetricRow(rows, "Calmar", mc.getSummary("Calmar"), false, false);

            // Special rows
            rows.add(new MonteCarloMetricRow("Prob. Loss", formatPercentage(mc.probLoss), "-", "-", "-", "-", "-"));
            rows.add(new MonteCarloMetricRow("ES (5%) Net", formatCurrency(mc.expectedShortfall5), "-", "-", "-", "-", "-"));

            mcMetricsTable.setItems(FXCollections.observableArrayList(rows));
        } catch (Exception ex) {
            logger.error("Failed to update MC metrics table", ex);
        }
    }

    private void addMetricRow(List<MonteCarloMetricRow> rows, String name, Backtester.strategies.StatSummary s, boolean currency, boolean percent) {
        if (s == null) return;
        String fmtMean = currency ? formatCurrency(s.mean) : percent ? formatPercentage(s.mean) : formatDecimal(s.mean);
        String fmtMedian = currency ? formatCurrency(s.median) : percent ? formatPercentage(s.median) : formatDecimal(s.median);
        String fmtP5 = currency ? formatCurrency(s.p5) : percent ? formatPercentage(s.p5) : formatDecimal(s.p5);
        String fmtP25 = currency ? formatCurrency(s.p25) : percent ? formatPercentage(s.p25) : formatDecimal(s.p25);
        String fmtP75 = currency ? formatCurrency(s.p75) : percent ? formatPercentage(s.p75) : formatDecimal(s.p75);
        String fmtP95 = currency ? formatCurrency(s.p95) : percent ? formatPercentage(s.p95) : formatDecimal(s.p95);
        rows.add(new MonteCarloMetricRow(name, fmtMean, fmtMedian, fmtP5, fmtP25, fmtP75, fmtP95));
    }

    private void updateMonteCarloPaths(MonteCarloResult mc) {
        if (mcPathsContainer == null || lastDates == null || lastDates.isEmpty()) return;
        if (mc.eqMean == null || mc.eqMean.length == 0) return;

        mcPathsContainer.getChildren().clear();

        TimeSeriesCollection dataset = new TimeSeriesCollection();

        TimeSeries mean = new TimeSeries("Mean Equity");
        TimeSeries p5 = new TimeSeries("P5");
        TimeSeries p25 = new TimeSeries("P25");
        TimeSeries p50 = new TimeSeries("Median");
        TimeSeries p75 = new TimeSeries("P75");
        TimeSeries p95 = new TimeSeries("P95");

        int len = Math.min(lastDates.size(), mc.eqMean.length);
        for (int i = 0; i < len; i++) {
            LocalDate dte = lastDates.get(i);
            Day d = new Day(dte.getDayOfMonth(), dte.getMonthValue(), dte.getYear());
            mean.addOrUpdate(d, mc.eqMean[i]);
            p5.addOrUpdate(d, mc.eqP5[i]);
            p25.addOrUpdate(d, mc.eqP25[i]);
            p50.addOrUpdate(d, mc.eqP50[i]);
            p75.addOrUpdate(d, mc.eqP75[i]);
            p95.addOrUpdate(d, mc.eqP95[i]);
        }

        dataset.addSeries(mean);
        dataset.addSeries(p5);
        dataset.addSeries(p25);
        dataset.addSeries(p50);
        dataset.addSeries(p75);
        dataset.addSeries(p95);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,
                "Date",
                "Equity",
                dataset,
                true,
                false,
                false
        );

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        XYPlot plot = chart.getXYPlot();
        plot.setRenderer(renderer);

        // Styling: mean bold, percentiles lighter
        renderer.setSeriesPaint(0, new Color(33, 150, 243));
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        Color light = new Color(33, 150, 243, 120);
        for (int i = 1; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, light);
            renderer.setSeriesStroke(i, new BasicStroke(1.0f));
        }

        // Interactive viewer (zoom/pan)
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        ChartViewer viewer = new ChartViewer(chart);
        viewer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        viewer.prefWidthProperty().bind(mcPathsContainer.widthProperty());
        viewer.prefHeightProperty().bind(mcPathsContainer.heightProperty());
        VBox.setVgrow(viewer, Priority.ALWAYS);
        mcPathsContainer.getChildren().setAll(viewer);
    }

//    private void updateMonteCarloResults(MonteCarloResult mc) {
//        if (mcNetProfitLabel != null) mcNetProfitLabel.setText(formatCurrency(mc.avgNetProfit));
//        if (mcMaxDrawdownLabel != null) mcMaxDrawdownLabel.setText(formatPercentage(mc.avgMaxDrawdown));
//        if (mcSharpeLabel != null) mcSharpeLabel.setText(formatDecimal(mc.avgSharpe));
//        if (mcEntriesLabel != null) mcEntriesLabel.setText(String.valueOf(Math.round(mc.avgEntries)));
//        if (mcOpenTradesLabel != null) mcOpenTradesLabel.setText(String.valueOf(Math.round(mc.avgOpenTrades)));
//        if (mcClosedTradesLabel != null) mcClosedTradesLabel.setText(String.valueOf(Math.round(mc.avgClosedTrades)));
//        if (mcWinnersLabel != null) mcWinnersLabel.setText(String.valueOf(Math.round(mc.avgWinners)));
//        if (mcLosersLabel != null) mcLosersLabel.setText(String.valueOf(Math.round(mc.avgLosers)));
//    }

    private void setLabelColor(Label label, double value) {
        if (Double.isNaN(value)) {
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        } else if (value > 0) {
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60;");
        } else if (value < 0) {
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
        } else {
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        }
    }

    @FXML
    private void stopBacktest() {
        isRunning = false;
        resetUI();
        statusLabel.setText("Backtest stopped by user");
    }

    private void resetUI() {
        isRunning = false;
        runButton.setDisable(false);
        stopButton.setDisable(true);
        progressBar.setVisible(false);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
