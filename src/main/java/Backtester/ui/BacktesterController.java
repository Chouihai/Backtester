package Backtester.ui;

import Backtester.caches.BarCache;
import Backtester.objects.Bar;
import Backtester.objects.Trade;
import Backtester.script.tokens.Parser;
import Backtester.services.HistoricalDataService;
import Backtester.strategies.StrategyRunner;
import Backtester.trades.PositionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
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
    private final PositionManager positionManager;

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
    public ProgressBar progressBar;
    public Label statusLabel;

    private ObservableList<Trade> tradesData = FXCollections.observableArrayList();
    private volatile boolean isRunning = false;

    public BacktesterController(HistoricalDataService historicalDataService, 
                               StrategyRunner strategyRunner,
                                PositionManager positionManager) {
        this.historicalDataService = historicalDataService;
        this.strategyRunner = strategyRunner;
        this.positionManager = positionManager;
    }

    @FXML
    public void initialize() {
        setupUI();
        setupEventHandlers();
    }

    private void setupUI() {
        startDatePicker.setValue(LocalDate.now().minusMonths(6));
        endDatePicker.setValue(LocalDate.now());
        initialCapitalField.setText("100000");
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
            List<Bar> bars = historicalDataService.getHistoricalData(symbol);
            
            if (bars.isEmpty()) {
                throw new RuntimeException("No data available for " + symbol + ". Please check the symbol and try again.");
            }

            // Validate that the requested dates are within the available data range
            LocalDate availableStartDate = bars.getFirst().date;
            LocalDate availableEndDate = bars.getLast().date;

            logger.info("Fetched " + bars.size() + " time series values for " + symbol + " from " + availableStartDate + " to " + availableEndDate + ".");
            
            if (startDate.isBefore(availableStartDate)) {
                throw new RuntimeException(String.format(
                    "Start date %s is before the earliest available data (%s). " +
                    "Please select a start date on or after %s.", 
                    startDate, availableStartDate, availableStartDate));
            }
            
            if (endDate.isAfter(availableEndDate)) {
                throw new RuntimeException(String.format(
                    "End date %s is after the latest available data (%s). " +
                    "Please select an end date on or before %s.", 
                    endDate, availableEndDate, availableEndDate));
            }
            
            // Check if the dates exist in the data (some dates might be missing due to weekends/holidays)
            BarCache barCache = new BarCache();
            barCache.loadCache(bars);
            
            Platform.runLater(() -> statusLabel.setText("Running strategy..."));

            if (!strategyScript.trim().isEmpty()) {
                try {
                    strategyRunner.run(strategyScript, startDate, endDate);
                } catch (Exception e) {
                    throw new RuntimeException("Strategy parsing failed: " + e.getMessage());
                }
            }

            // Update UI with results
            Platform.runLater(() -> {
                updateResults();
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

    private void updateResults() {
        tradesData.clear();
        tradesData.addAll(positionManager.allTrades());

        double netProfit = positionManager.netProfit();
        double grossProfit = positionManager.grossProfit();
        double grossLoss = positionManager.grossLoss();
        double openPnL = positionManager.openPnL();
        double maxDrawdown = positionManager.maxDrawdown();
        double maxRunUp = positionManager.maxRunUp();
        double sharpeRatio = positionManager.sharpeRatio();

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

        updateChart();
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

    public double openPnl(Trade trade) {
        return positionManager.openPnl(trade);
    }

    private void updateChart() {
        // Placeholder for chart update
        // TODO: Implement chart visualization
        if (chartContainer != null) {
            chartContainer.getChildren().clear();
            Label chartLabel = new Label("Chart visualization coming soon...");
            chartLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            chartContainer.getChildren().add(chartLabel);
        }
    }

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