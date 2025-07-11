package Backtester.ui;

import Backtester.objects.Trade;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
//        VBox chartSection = createChartSection();
        
        // 4. Performance Metrics Section
        VBox performanceSection = createPerformanceSection();
        
        // 5. Trade History Section
        VBox tradeHistorySection = createTradeHistorySection();
        
        // Add all sections to main layout
        mainLayout.getChildren().addAll(
            strategySection,
            inputSection,
//            chartSection,
            performanceSection,
            tradeHistorySection
        );
        
        // Create scroll pane for the main layout
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Initialize the controller to set up event handlers
        controller.initialize();
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
        
//        // Strategy controls later
//        HBox strategyControls = new HBox(10);
//        Button loadButton = new Button("Load Strategy");
//        Button saveButton = new Button("Save Strategy");
//        Button clearButton = new Button("Clear");
//
//        strategyControls.getChildren().addAll(loadButton, saveButton, clearButton);
//        strategyControls.setAlignment(Pos.CENTER_RIGHT);
        
        // Connect to controller
        controller.strategyTextArea = strategyArea;
        
        section.getChildren().addAll(title, strategyArea);
        return section;
    }

//    private VBox createChartSection() {
//        VBox section = new VBox(10);
//        section.setPadding(new Insets(10));
//        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
//
//        Label title = new Label("Price Chart & Strategy Performance");
//        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
//
//        // Placeholder for chart
//        VBox chartContainer = new VBox();
//        chartContainer.setPrefHeight(300);
//        chartContainer.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-style: dashed;");
//        chartContainer.setAlignment(Pos.CENTER);
//
//        Label placeholderLabel = new Label("Chart will be displayed here");
//        placeholderLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");
//
//        chartContainer.getChildren().add(placeholderLabel);
//
//        // Connect to controller
//        controller.chartContainer = chartContainer;
//
//        section.getChildren().addAll(title, chartContainer);
//        return section;
//    }

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
        
        // Create metric displays (value + percent)
        Label netProfitLabel = new Label("Net Profit:");
        netProfitLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label netProfitValue = new Label("$0.00 (0%)");
        netProfitValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label grossProfitLabel = new Label("Gross Profit:");
        grossProfitLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label grossProfitValue = new Label("$0.00 (0%)");
        grossProfitValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label grossLossLabel = new Label("Gross Loss:");
        grossLossLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label grossLossValue = new Label("$0.00 (0%)");
        grossLossValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label openPnLLabel = new Label("Open PnL:");
        openPnLLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label openPnLValue = new Label("$0.00 (0%)");
        openPnLValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label maxDrawdownLabel = new Label("Max Drawdown:");
        maxDrawdownLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label maxDrawdownValue = new Label("$0.00 (0%)");
        maxDrawdownValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label maxRunUpLabel = new Label("Max Run Up:");
        maxRunUpLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label maxRunUpValue = new Label("$0.00 (0%)");
        maxRunUpValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label sharpeRatioLabel = new Label("Sharpe Ratio:");
        sharpeRatioLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label sharpeRatioValue = new Label("0.00 (0%)");
        sharpeRatioValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        // Add to grid (2 rows, 4 columns)
        metricsGrid.add(netProfitLabel, 0, 0);
        metricsGrid.add(netProfitValue, 1, 0);
        metricsGrid.add(grossProfitLabel, 2, 0);
        metricsGrid.add(grossProfitValue, 3, 0);
        metricsGrid.add(grossLossLabel, 4, 0);
        metricsGrid.add(grossLossValue, 5, 0);
        metricsGrid.add(openPnLLabel, 6, 0);
        metricsGrid.add(openPnLValue, 7, 0);

        metricsGrid.add(maxDrawdownLabel, 0, 1);
        metricsGrid.add(maxDrawdownValue, 1, 1);
        metricsGrid.add(maxRunUpLabel, 2, 1);
        metricsGrid.add(maxRunUpValue, 3, 1);
        metricsGrid.add(sharpeRatioLabel, 4, 1);
        metricsGrid.add(sharpeRatioValue, 5, 1);
        // Optionally leave columns 6,7 blank for symmetry

        // Connect to controller
        controller.netProfitLabel = netProfitValue;
        controller.grossProfitLabel = grossProfitValue;
        controller.grossLossLabel = grossLossValue;
        controller.openPnLLabel = openPnLValue;
        controller.maxDrawdownLabel = maxDrawdownValue;
        controller.maxRunUpLabel = maxRunUpValue;
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
        TableColumn<Trade, String> entryDateCol = new TableColumn<>("Entry Date");
        entryDateCol.setPrefWidth(100);
        entryDateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEntryBarDate().toString()));
        
        TableColumn<Trade, Double> entryPriceCol = new TableColumn<>("Entry Price");
        entryPriceCol.setPrefWidth(100);
        entryPriceCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleDoubleProperty(data.getValue().entry.open).asObject());
        entryPriceCol.setCellFactory(column -> new TableCell<Trade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", item));
                }
            }
        });
        
        TableColumn<Trade, String> exitDateCol = new TableColumn<>("Exit Date");
        exitDateCol.setPrefWidth(100);
        exitDateCol.setCellValueFactory(data -> {
            if (data.getValue().isOpen()) {
                return new javafx.beans.property.SimpleStringProperty("");
            } else {
                return new javafx.beans.property.SimpleStringProperty(data.getValue().getExit().get().date.toString());
            }
        });
        
        TableColumn<Trade, Double> exitPriceCol = new TableColumn<>("Exit Price");
        exitPriceCol.setPrefWidth(100);
        exitPriceCol.setCellValueFactory(data -> {
            if (data.getValue().isOpen()) {
                return new javafx.beans.property.SimpleDoubleProperty(0.0).asObject();
            } else {
                return new javafx.beans.property.SimpleDoubleProperty(data.getValue().getExit().get().open).asObject();
            }
        });
        exitPriceCol.setCellFactory(column -> new TableCell<Trade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", item));
                }
            }
        });
        
        TableColumn<Trade, String> sideCol = new TableColumn<>("Side");
        sideCol.setPrefWidth(80);
        sideCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getDirection().toString()));
        
        TableColumn<Trade, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setPrefWidth(100);
        quantityCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        
        TableColumn<Trade, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().isOpen() ? "Open" : "Closed"));

        TableColumn<Trade, Double> pnlCol = new TableColumn<>("PnL");
        pnlCol.setPrefWidth(100);
        pnlCol.setCellValueFactory(data -> {
            Trade trade = data.getValue();
            if (trade.isOpen()) {
                // For open trades, we'll need to get the current bar from the controller
                return new javafx.beans.property.SimpleDoubleProperty(controller.openPnl(trade)).asObject();
            } else {
                return new javafx.beans.property.SimpleDoubleProperty(trade.profit()).asObject();
            }
        });
        pnlCol.setCellFactory(column -> new TableCell<Trade, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", item));
                }
            }
        });
        
        tradeTable.getColumns().addAll(entryDateCol, entryPriceCol, exitDateCol, exitPriceCol, sideCol, quantityCol, statusCol, pnlCol);
        
        // Add placeholder
        tradeTable.setPlaceholder(new Label("No trades to display. Run a backtest to see results."));
        
        // Connect to controller
        controller.tradesTable = tradeTable;
        
        section.getChildren().addAll(title, tradeTable);
        return section;
    }
} 