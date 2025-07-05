package Backtester.ui;

import Backtester.objects.Bar;
import Backtester.objects.Security;
import Backtester.objects.Trade;
import Backtester.script.ScriptEvaluator;
import Backtester.services.HistoricalDataService;
import Backtester.strategies.StrategyRunner;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final StrategyRunner strategyRunner;
    private final ScriptEvaluator scriptEvaluator;

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
    public Label totalReturnLabel;
    public Label maxDrawdownLabel;
    public Label sharpeRatioLabel;
    public ProgressBar progressBar;
    public Label statusLabel;

    // Data
    private ObservableList<Trade> tradesData = FXCollections.observableArrayList();
    private volatile boolean isRunning = false;

    public BacktesterController(HistoricalDataService historicalDataService, 
                               StrategyRunner strategyRunner, 
                               ScriptEvaluator scriptEvaluator) {
        this.historicalDataService = historicalDataService;
        this.strategyRunner = strategyRunner;
        this.scriptEvaluator = scriptEvaluator;
    }

    @FXML
    public void initialize() {
        setupUI();
        setupEventHandlers();
    }

    private void setupUI() {
        // Initialize date pickers with default values
        startDatePicker.setValue(LocalDate.now().minusMonths(6));
        endDatePicker.setValue(LocalDate.now());
        
        // Initialize initial capital
        initialCapitalField.setText("100000");
        
        // Setup trades table
        tradesTable.setItems(tradesData);
        
        // Setup progress bar
        progressBar.setVisible(false);
        
        // Setup status
        statusLabel.setText("Ready to run backtest");
        
        // Disable stop button initially
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

        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Start backtest in background thread
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
            // Get inputs
            String symbol = symbolField.getText().trim().toUpperCase();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            double initialCapital = Double.parseDouble(initialCapitalField.getText());
            String strategyScript = strategyTextArea.getText();

            // Fetch historical data
            Platform.runLater(() -> statusLabel.setText("Fetching data for " + symbol + "..."));
            List<Bar> bars = historicalDataService.getHistoricalData(symbol, startDate, endDate);
            
            if (bars.isEmpty()) {
                throw new RuntimeException("No data available for " + symbol + " in the specified date range");
            }

            Platform.runLater(() -> statusLabel.setText("Running strategy..."));
            
            // Create security object (using a simple constructor for now)
            Security security = new Security(1, symbol, symbol, "NYSE", java.time.LocalDateTime.now());
            
            // Parse and validate strategy
            if (!strategyScript.trim().isEmpty()) {
                try {
                    // Create evaluation context for validation
                    Backtester.script.EvaluationContext context = new Backtester.script.EvaluationContext(bars.get(0));
                    scriptEvaluator.evaluate(context);
                } catch (Exception e) {
                    throw new RuntimeException("Strategy parsing failed: " + e.getMessage());
                }
            }

            // Run strategy (simplified for now - will need to implement proper strategy running)
            List<Trade> trades = new ArrayList<>(); // Placeholder - need to implement actual strategy running
            
            // Update UI with results
            Platform.runLater(() -> {
                updateResults(trades, bars, initialCapital);
                statusLabel.setText("Backtest completed successfully");
                resetUI();
            });

        } catch (Exception e) {
            logger.error("Backtest failed", e);
            Platform.runLater(() -> {
                showAlert("Backtest Failed", "Error: " + e.getMessage());
                resetUI();
            });
        }
    }

    private boolean validateInputs() {
        // Validate symbol
        if (symbolField.getText().trim().isEmpty()) {
            showAlert("Invalid Input", "Please enter a stock symbol.");
            return false;
        }

        // Validate dates
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showAlert("Invalid Input", "Please select both start and end dates.");
            return false;
        }

        if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
            showAlert("Invalid Input", "Start date must be before end date.");
            return false;
        }

        // Validate initial capital
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

    private void updateResults(List<Trade> trades, List<Bar> bars, double initialCapital) {
        // Update trades table
        tradesData.clear();
        tradesData.addAll(trades);

        // Calculate performance metrics
        double totalReturn = calculateTotalReturn(trades, initialCapital);
        double maxDrawdown = calculateMaxDrawdown(trades, bars);
        double sharpeRatio = calculateSharpeRatio(trades, bars);

        // Update performance labels
        totalReturnLabel.setText(String.format("%.2f%%", totalReturn * 100));
        maxDrawdownLabel.setText(String.format("%.2f%%", maxDrawdown * 100));
        sharpeRatioLabel.setText(String.format("%.2f", sharpeRatio));

        // Update chart (placeholder for now)
        updateChart(bars, trades);
    }

    private double calculateTotalReturn(List<Trade> trades, double initialCapital) {
        if (trades.isEmpty()) return 0.0;
        
        double finalValue = initialCapital;
        for (Trade trade : trades) {
            if (trade.getDirection() == Trade.TradeDirection.LONG) {
                finalValue -= trade.getQuantity() * trade.entry.open;
            } else {
                finalValue += trade.getQuantity() * trade.entry.open;
            }
        }
        
        return (finalValue - initialCapital) / initialCapital;
    }

    private double calculateMaxDrawdown(List<Trade> trades, List<Bar> bars) {
        // Simplified max drawdown calculation
        if (trades.isEmpty() || bars.isEmpty()) return 0.0;
        
        double peak = bars.get(0).close;
        double maxDrawdown = 0.0;
        
        for (Bar bar : bars) {
            if (bar.close > peak) {
                peak = bar.close;
            }
            double drawdown = (peak - bar.close) / peak;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }
        
        return maxDrawdown;
    }

    private double calculateSharpeRatio(List<Trade> trades, List<Bar> bars) {
        // Simplified Sharpe ratio calculation
        if (trades.isEmpty() || bars.size() < 2) return 0.0;
        
        // Calculate daily returns
        double[] returns = new double[bars.size() - 1];
        for (int i = 1; i < bars.size(); i++) {
            returns[i - 1] = (bars.get(i).close - bars.get(i - 1).close) / bars.get(i - 1).close;
        }
        
        // Calculate mean and standard deviation
        double sum = 0.0;
        for (double ret : returns) {
            sum += ret;
        }
        double mean = sum / returns.length;
        
        double variance = 0.0;
        for (double ret : returns) {
            variance += Math.pow(ret - mean, 2);
        }
        variance /= returns.length;
        double stdDev = Math.sqrt(variance);
        
        // Annualized Sharpe ratio (assuming daily data)
        return stdDev == 0 ? 0 : (mean * 252) / (stdDev * Math.sqrt(252));
    }

    private void updateChart(List<Bar> bars, List<Trade> trades) {
        // Placeholder for chart update
        // TODO: Implement chart visualization
        chartContainer.getChildren().clear();
        Label chartLabel = new Label("Chart visualization coming soon...");
        chartLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        chartContainer.getChildren().add(chartLabel);
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