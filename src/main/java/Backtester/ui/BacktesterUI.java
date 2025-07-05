package Backtester.ui;

import Backtester.objects.Trade;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;

public class BacktesterUI {

    private final BacktesterController controller;

    public BacktesterUI(BacktesterController controller) {
        this.controller = controller;
    }

    public Parent createUI() {
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
        
        return scrollPane;
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
        
        // Progress bar and status
        ProgressBar progressBar = new ProgressBar();
        progressBar.setVisible(false);
        progressBar.setPrefWidth(200);
        
        Label statusLabel = new Label("Ready to run backtest");
        statusLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        
        HBox buttonBox = new HBox(10, runButton, stopButton, progressBar, statusLabel);
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
        
        grid.add(buttonBox, 0, 2, 4, 1);
        
        // Connect to controller
        controller.symbolField = securityField;
        controller.startDatePicker = startDatePicker;
        controller.endDatePicker = endDatePicker;
        controller.initialCapitalField = capitalField;
        controller.runButton = runButton;
        controller.stopButton = stopButton;
        controller.progressBar = progressBar;
        controller.statusLabel = statusLabel;
        
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
        
        // Connect to controller
        controller.strategyTextArea = strategyArea;
        
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
        VBox chartContainer = new VBox();
        chartContainer.setPrefHeight(300);
        chartContainer.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-style: dashed;");
        chartContainer.setAlignment(Pos.CENTER);
        
        Label placeholderLabel = new Label("Chart will be displayed here");
        placeholderLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");
        
        chartContainer.getChildren().add(placeholderLabel);
        
        // Connect to controller
        controller.chartContainer = chartContainer;
        
        section.getChildren().addAll(title, chartContainer);
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
        Label totalReturnLabel = new Label("Total Return:");
        totalReturnLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label totalReturnValue = new Label("0.00%");
        totalReturnValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        Label maxDrawdownLabel = new Label("Max Drawdown:");
        maxDrawdownLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label maxDrawdownValue = new Label("0.00%");
        maxDrawdownValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        Label sharpeRatioLabel = new Label("Sharpe Ratio:");
        sharpeRatioLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label sharpeRatioValue = new Label("0.00");
        sharpeRatioValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        
        // Add to grid
        metricsGrid.add(totalReturnLabel, 0, 0);
        metricsGrid.add(totalReturnValue, 1, 0);
        metricsGrid.add(maxDrawdownLabel, 2, 0);
        metricsGrid.add(maxDrawdownValue, 3, 0);
        
        metricsGrid.add(sharpeRatioLabel, 0, 1);
        metricsGrid.add(sharpeRatioValue, 1, 1);
        
        // Connect to controller
        controller.totalReturnLabel = totalReturnValue;
        controller.maxDrawdownLabel = maxDrawdownValue;
        controller.sharpeRatioLabel = sharpeRatioValue;
        
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
        dateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEntryBarDate().toString()));
        
        TableColumn<Trade, String> sideCol = new TableColumn<>("Side");
        sideCol.setPrefWidth(80);
        sideCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getDirection().toString()));
        
        TableColumn<Trade, Double> priceCol = new TableColumn<>("Price");
        priceCol.setPrefWidth(100);
        priceCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleDoubleProperty(data.getValue().entry.open).asObject());
        
        TableColumn<Trade, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setPrefWidth(100);
        quantityCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        
        TableColumn<Trade, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().isOpen() ? "Open" : "Closed"));
        
        tradeTable.getColumns().addAll(dateCol, sideCol, priceCol, quantityCol, statusCol);
        
        // Add placeholder
        tradeTable.setPlaceholder(new Label("No trades to display. Run a backtest to see results."));
        
        // Connect to controller
        controller.tradesTable = tradeTable;
        
        section.getChildren().addAll(title, tradeTable);
        return section;
    }
} 