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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
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
    public CodeMirrorStrategyEditor cmEditor;
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

    public Label mcTitleLabel;
    public TableView<MonteCarloMetricRow> mcMetricsTable;

    private ChartManager chartManager;
    private MonteCarloMetricsTable mcTable;
    private WebView equityWebView;
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
        startDatePicker.setValue(LocalDate.of(2010, 1, 1));
        endDatePicker.setValue(LocalDate.of(2025, 1, 1));
        if (symbolField != null) {
            symbolField.setText("AAPL");
        }
        String defaultStrategy = """
                sma20 = sma(20)
                sma50 = sma(50)

                if (crossover(sma20, sma50)):
                    createOrder("long", true, 1000)
                if (crossover(sma50, sma20)):
                    createOrder("position1", false, 1000)
                """;
        if (cmEditor != null) { cmEditor.setText(defaultStrategy); }
        initialCapitalField.setText("100000");
        if (permutationsField != null) permutationsField.setText("100");
        tradesTable.setItems(tradesData);
        progressBar.setVisible(false);
        statusLabel.setText("Ready to run backtest");
        stopButton.setDisable(true);

        // Initialize managers
        equityWebView = new WebView();
        chartManager = new ChartManager(equityWebView, chartContainer, statusLabel);
        if (mcMetricsTable != null) {
            mcTable = new MonteCarloMetricsTable(mcMetricsTable);
        }
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

        final String strategyScriptSnapshot = getStrategyScript();

        CompletableFuture.runAsync(() -> {
            try {
                runBacktestAsync(strategyScriptSnapshot);
            } catch (Exception e) {
                logger.error("Backtest failed", e);
                Platform.runLater(() -> {
                    showAlert("Backtest Failed", "Error: " + e.getMessage());
                    resetUI();
                });
            }
        }, executorService);
    }

    private void runBacktestAsync(String strategyScript) {
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
                    long baseStartNs = System.nanoTime();
                    StrategyRunner strategyRunner = new StrategyRunner(bars, lookbackBars, strategyScript, logger);
                    results = strategyRunner.run(initialCapital);
                    long baseEndNs = System.nanoTime();
                    Duration baseDur = Duration.ofNanos(baseEndNs - baseStartNs);
                    logger.info("Base strategy runtime: {} ms ({} s)", baseDur.toMillis(), String.format("%.3f", baseDur.toMillis() / 1000.0));
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
                int threads = Math.min(4, permutations);
                long mcStartNs = System.nanoTime();
                MonteCarloResult mc = mcRunner.run(permutations, threads, initialCapital);
                long mcEndNs = System.nanoTime();
                Duration mcDur = Duration.ofNanos(mcEndNs - mcStartNs);
                logger.info("Monte Carlo runtime: {} permutations in {} ms ({} s) using {} threads",
                        permutations,
                        mcDur.toMillis(),
                        String.format("%.3f", mcDur.toMillis() / 1000.0),
                        threads);
                Platform.runLater(() -> {
                    if (mcTitleLabel != null) {
                        mcTitleLabel.setText("Monte Carlo Metrics");
                    }
                    updateMonteCarloResults(mc);
                    // Overlay MC mean on the main equity chart
                    if (chartManager != null) {
                        chartManager.overlayMonteCarlo(mc, lastDates, null, buyAndHoldEquity);
                    }
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

        if (getStrategyScript().trim().isEmpty()) {
            showAlert("Invalid Input", "Cannot have an empty strategy");
            return false;
        }

        try {
            Parser parser = new Parser();
            String s = getStrategyScript().trim();
            parser.parse(s);
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

    private String getStrategyScript() {
        if (cmEditor != null) return cmEditor.getText();
        return "";
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

        netProfitLabel.setText(UiFormat.formatCurrency(netProfit));
        UiFormat.setLabelColor(netProfitLabel, netProfit);
        
        grossProfitLabel.setText(UiFormat.formatCurrency(grossProfit));
        grossProfitLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #27ae60;"); // Always green
        
        grossLossLabel.setText(UiFormat.formatCurrency(grossLoss));
        grossLossLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e74c3c;"); // Always red
        
        openPnLLabel.setText(UiFormat.formatCurrency(openPnL));
        UiFormat.setLabelColor(openPnLLabel, openPnL);
        
        maxDrawdownLabel.setText(UiFormat.formatPercentage(maxDrawdown));
        UiFormat.setLabelColor(maxDrawdownLabel, maxDrawdown);
        
        maxRunUpLabel.setText(UiFormat.formatPercentage(maxRunUp));
        UiFormat.setLabelColor(maxRunUpLabel, maxRunUp);
        
        sharpeRatioLabel.setText(UiFormat.formatDecimal(sharpeRatio));
        UiFormat.setLabelColor(sharpeRatioLabel, sharpeRatio);

        if (tradesMadeLabel != null) tradesMadeLabel.setText(String.valueOf(tradesMade));
        if (winningTradesLabel != null) winningTradesLabel.setText(String.valueOf(winningTrades));

        this.lastDates = dates;
        updateChart(results, dates, buyAndHold);
    }

    private int parseOrDefault(String text, int def) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private void updateChart(RunResult result, List<LocalDate> dates, List<Double> buyAndHold) {
        if (chartManager == null) return;
        var strat = result.strategyEquity();
        chartManager.updateEquity(dates, strat, buyAndHold);
    }

    private void updateMonteCarloResults(MonteCarloResult mc) {
        if (mcTable == null) return;
        try {
            mcTable.populate(mc);
        } catch (Exception ex) {
            logger.error("Failed to update MC metrics table", ex);
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

