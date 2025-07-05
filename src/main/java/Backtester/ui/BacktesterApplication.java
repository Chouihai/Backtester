package Backtester.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class BacktesterApplication extends Application {

    private Injector injector;

    @Override
    public void init() throws Exception {
        super.init();
        // Initialize dependency injection with simplified module
        injector = Guice.createInjector(new AppModule());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Backtester - Stock Trading Strategy Backtester");
        
        // Create main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");
        
        // 1. Input Controls Section
        VBox inputSection = createInputSection();
        
        // 2. Strategy Editor Section
        VBox strategySection = createStrategySection();
        
        // 3. Chart Section (placeholder)
        VBox chartSection = createChartSection();
        
        // 4. Performance Metrics Section
        VBox performanceSection = createPerformanceSection();
        
        // 5. Trade History Section
        VBox tradeHistorySection = createTradeHistorySection();
        
        // Add all sections to main layout
        mainLayout.getChildren().addAll(
            inputSection,
            strategySection,
            chartSection,
            performanceSection,
            tradeHistorySection
        );
        
        // Create scroll pane for the main layout
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        Scene scene = new Scene(scrollPane, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    private VBox createInputSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label title = new Label("Backtest Configuration");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        // Input fields in a grid
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);
        
        // Security input
        Label securityLabel = new Label("Security Symbol:");
        securityLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        TextField securityField = new TextField();
        securityField.setPromptText("e.g., AAPL, MSFT, GOOGL");
        securityField.setPrefWidth(150);
        securityField.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        
        // Date inputs
        Label startDateLabel = new Label("Start Date:");
        startDateLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPrefWidth(150);
        startDatePicker.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        
        Label endDateLabel = new Label("End Date:");
        endDateLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPrefWidth(150);
        endDatePicker.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        
        // Initial capital
        Label capitalLabel = new Label("Initial Capital ($):");
        capitalLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        TextField capitalField = new TextField();
        capitalField.setPromptText("100000");
        capitalField.setPrefWidth(150);
        capitalField.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        
        // Control buttons
        Button runButton = new Button("Run Backtest");
        runButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        runButton.setPrefWidth(120);
        
        Button stopButton = new Button("Stop");
        stopButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        stopButton.setPrefWidth(80);
        stopButton.setDisable(true);
        
        HBox buttonBox = new HBox(10, runButton, stopButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Add to grid
        grid.add(securityLabel, 0, 0);
        grid.add(securityField, 1, 0);
        grid.add(startDateLabel, 2, 0);
        grid.add(startDatePicker, 3, 0);
        
        grid.add(endDateLabel, 0, 1);
        grid.add(endDatePicker, 1, 1);
        grid.add(capitalLabel, 2, 1);
        grid.add(capitalField, 3, 1);
        
        grid.add(buttonBox, 3, 2);
        
        section.getChildren().addAll(title, grid);
        return section;
    }

    private VBox createStrategySection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label title = new Label("Strategy Editor");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        TextArea strategyArea = new TextArea();
        strategyArea.setPromptText("Enter your trading strategy here...\n\nExample:\nif sma(20) > sma(50) then\n  createOrder(\"BUY\", 100)\nend\n\nif crossover(sma(20), sma(50)) then\n  createOrder(\"SELL\", 100)\nend");
        strategyArea.setPrefRowCount(8);
        strategyArea.setWrapText(true);
        strategyArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px;");
        
        // Strategy controls
        HBox strategyControls = new HBox(10);
        Button loadButton = new Button("Load Strategy");
        Button saveButton = new Button("Save Strategy");
        Button clearButton = new Button("Clear");
        
        strategyControls.getChildren().addAll(loadButton, saveButton, clearButton);
        strategyControls.setAlignment(Pos.CENTER_RIGHT);
        
        section.getChildren().addAll(title, strategyArea, strategyControls);
        return section;
    }

    private VBox createChartSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label title = new Label("Price Chart & Strategy Performance");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        // Placeholder for chart
        Pane chartPlaceholder = new Pane();
        chartPlaceholder.setPrefHeight(300);
        chartPlaceholder.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-style: dashed;");
        
        Label placeholderLabel = new Label("Chart will be displayed here");
        placeholderLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");
        placeholderLabel.setAlignment(Pos.CENTER);
        placeholderLabel.setLayoutX(400);
        placeholderLabel.setLayoutY(140);
        
        chartPlaceholder.getChildren().add(placeholderLabel);
        
        section.getChildren().addAll(title, chartPlaceholder);
        return section;
    }

    private VBox createPerformanceSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label title = new Label("Performance Metrics");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        // Performance metrics grid
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(20);
        metricsGrid.setVgap(10);
        metricsGrid.setAlignment(Pos.CENTER_LEFT);
        
        // Create metric displays
        Label totalPnLLabel = new Label("Total P&L:");
        totalPnLLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label totalPnLValue = new Label("$0.00");
        totalPnLValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        Label openPnLLabel = new Label("Open P&L:");
        openPnLLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label openPnLValue = new Label("$0.00");
        openPnLValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        Label totalTradesLabel = new Label("Total Trades:");
        totalTradesLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label totalTradesValue = new Label("0");
        totalTradesValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        Label winRateLabel = new Label("Win Rate:");
        winRateLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label winRateValue = new Label("0%");
        winRateValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        Label sharpeRatioLabel = new Label("Sharpe Ratio:");
        sharpeRatioLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label sharpeRatioValue = new Label("0.00");
        sharpeRatioValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        Label maxDrawdownLabel = new Label("Max Drawdown:");
        maxDrawdownLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label maxDrawdownValue = new Label("0%");
        maxDrawdownValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        // Add to grid
        metricsGrid.add(totalPnLLabel, 0, 0);
        metricsGrid.add(totalPnLValue, 1, 0);
        metricsGrid.add(openPnLLabel, 2, 0);
        metricsGrid.add(openPnLValue, 3, 0);
        
        metricsGrid.add(totalTradesLabel, 0, 1);
        metricsGrid.add(totalTradesValue, 1, 1);
        metricsGrid.add(winRateLabel, 2, 1);
        metricsGrid.add(winRateValue, 3, 1);
        
        metricsGrid.add(sharpeRatioLabel, 0, 2);
        metricsGrid.add(sharpeRatioValue, 1, 2);
        metricsGrid.add(maxDrawdownLabel, 2, 2);
        metricsGrid.add(maxDrawdownValue, 3, 2);
        
        section.getChildren().addAll(title, metricsGrid);
        return section;
    }

    private VBox createTradeHistorySection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label title = new Label("Trade History");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        // Create table
        TableView<Trade> tradeTable = new TableView<>();
        tradeTable.setPrefHeight(200);
        
        // Define columns
        TableColumn<Trade, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(100);
        
        TableColumn<Trade, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(80);
        
        TableColumn<Trade, Double> priceCol = new TableColumn<>("Price");
        priceCol.setPrefWidth(100);
        
        TableColumn<Trade, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setPrefWidth(100);
        
        TableColumn<Trade, Double> pnlCol = new TableColumn<>("P&L");
        pnlCol.setPrefWidth(100);
        
        TableColumn<Trade, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        
        tradeTable.getColumns().addAll(dateCol, typeCol, priceCol, quantityCol, pnlCol, statusCol);
        
        // Add some sample data (will be replaced with real data)
        tradeTable.setPlaceholder(new Label("No trades to display. Run a backtest to see results."));
        
        section.getChildren().addAll(title, tradeTable);
        return section;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println("Application shutting down...");
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    // Simple Trade class for the table (placeholder)
    public static class Trade {
        private String date;
        private String type;
        private double price;
        private int quantity;
        private double pnl;
        private String status;
        
        // Constructor and getters would go here
    }
} 