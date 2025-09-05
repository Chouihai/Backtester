package Backtester.ui;

import Backtester.objects.Bar;
import Backtester.objects.Trade;
import Backtester.script.tokens.Parser;
import Backtester.services.HistoricalDataService;
import Backtester.strategies.RunResult;
import Backtester.strategies.StrategyRunner;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
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
    public ProgressBar progressBar;
    public Label statusLabel;
    public TextField permutationsField;
    public TextField blockSizeField;

    // Monte Carlo output labels
    public Label mcNetProfitLabel;
    public Label mcMaxDrawdownLabel;
    public Label mcSharpeLabel;
    public Label mcEntriesLabel;
    public Label mcOpenTradesLabel;
    public Label mcClosedTradesLabel;
    public Label mcWinnersLabel;
    public Label mcLosersLabel;
    public Bar lastBar;

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
        if (blockSizeField != null) blockSizeField.setText("10");
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
                    StrategyRunner strategyRunner = new StrategyRunner(bars, strategyScript, logger);
                    results = strategyRunner.run(initialCapital);
                } catch (Exception e) {
                    throw new RuntimeException("Strategy parsing failed: " + e.getMessage());
                }
            }

            // Update UI with results
            RunResult finalResults = results;
            Platform.runLater(() -> {
                if (finalResults != null) {
                    updateResults(finalResults, dates, buyAndHoldEquity);
                    statusLabel.setText("Backtest completed successfully");
                    resetUI();
                }
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

        // Render chart to image to avoid SwingNode/module export issues
        BufferedImage img = chart.createBufferedImage(1200, 320);
        Image fxImage = SwingFXUtils.toFXImage(img, null);
        ImageView imageView = new ImageView(fxImage);
        imageView.setPreserveRatio(true);
        // Fit inside fixed-height container and fill width
        imageView.fitHeightProperty().bind(chartContainer.heightProperty());
        imageView.fitWidthProperty().bind(chartContainer.widthProperty());
        chartContainer.getChildren().add(imageView);
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
