package Backtester.ui;

import Backtester.objects.Trade;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
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

        // 3. Chart Section
        VBox chartSection = createChartSection();
        
        // 4. Performance Metrics Section
        VBox performanceSection = createPerformanceSection();
        VBox mcSection = createMonteCarloSection();
        VBox mcPathsSection = createMonteCarloPathsSection();
        
        // 5. Trade History Section
        VBox tradeHistorySection = createTradeHistorySection();
        
        // Add all sections to main layout
        // Order sections so real-data stats and trades are grouped together
        mainLayout.getChildren().addAll(
            strategySection,
            inputSection,
            chartSection,
            performanceSection,
            tradeHistorySection,
            mcSection,
            mcPathsSection
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
        
        // Monte Carlo inputs
        Label permutationsLabel = new Label("Permutations:");
        permutationsLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        TextField permutationsField = new TextField();
        permutationsField.setPromptText("e.g., 100");
        permutationsField.setPrefWidth(120);
        permutationsField.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");

        // Block size removed (was used for bootstrap); Brownian bridge does not require it.

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
        
        grid.add(permutationsLabel, 0, 2);
        grid.add(permutationsField, 1, 2);
        // Removed block size controls

        grid.add(buttonBox, 0, 3, 4, 1);
        
        // Connect to controller
        controller.symbolField = securityField;
        controller.startDatePicker = startDatePicker;
        controller.endDatePicker = endDatePicker;
        controller.initialCapitalField = capitalField;
        controller.runButton = runButton;
        controller.stopButton = stopButton;
        controller.progressBar = progressBar;
        controller.statusLabel = statusLabel;
        controller.permutationsField = permutationsField;
        // Block size field removed
        
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

    private VBox createChartSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label title = new Label("Equity Curve");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        VBox chartContainer = new VBox();
        chartContainer.setMinHeight(320);
        chartContainer.setPrefHeight(320);
        chartContainer.setMaxHeight(320);
        // Inner container should be plain to avoid double borders/boxes
        chartContainer.setStyle("-fx-background-color: transparent;");
        chartContainer.setAlignment(Pos.CENTER);

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
        
        // Create metric displays (value + percent)
        Label netProfitLabel = new Label("Net Profit:");
        netProfitLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label netProfitValue = new Label("$0.00");
        // Label netProfitValue = new Label("$0.00 (0%)"); // TODO: Add percentage calculation
        netProfitValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label grossProfitLabel = new Label("Gross Profit:");
        grossProfitLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label grossProfitValue = new Label("$0.00");
        // Label grossProfitValue = new Label("$0.00 (0%)"); // TODO: Add percentage calculation
        grossProfitValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label grossLossLabel = new Label("Gross Loss:");
        grossLossLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label grossLossValue = new Label("$0.00");
        // Label grossLossValue = new Label("$0.00 (0%)"); // TODO: Add percentage calculation
        grossLossValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label openPnLLabel = new Label("Open PnL:");
        openPnLLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label openPnLValue = new Label("$0.00");
        // Label openPnLValue = new Label("$0.00 (0%)"); // TODO: Add percentage calculation
        openPnLValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label maxDrawdownLabel = new Label("Max Drawdown:");
        maxDrawdownLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label maxDrawdownValue = new Label("$0.00");
        // Label maxDrawdownValue = new Label("$0.00 (0%)"); // TODO: Add percentage calculation
        maxDrawdownValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label maxRunUpLabel = new Label("Max Run Up:");
        maxRunUpLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label maxRunUpValue = new Label("$0.00");
        // Label maxRunUpValue = new Label("$0.00 (0%)"); // TODO: Add percentage calculation
        maxRunUpValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label sharpeRatioLabel = new Label("Sharpe Ratio:");
        sharpeRatioLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label sharpeRatioValue = new Label("0.00");
        // Label sharpeRatioValue = new Label("0.00 (0%)"); // TODO: Add percentage calculation
        sharpeRatioValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        // Add to grid (now 3 rows)
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

        Label tradesMadeLabel = new Label("Trades Made:");
        tradesMadeLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label tradesMadeValue = new Label("0");
        tradesMadeValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        Label winningTradesLabel = new Label("Winning Trades:");
        winningTradesLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");
        Label winningTradesValue = new Label("0");
        winningTradesValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        metricsGrid.add(tradesMadeLabel, 0, 2);
        metricsGrid.add(tradesMadeValue, 1, 2);
        metricsGrid.add(winningTradesLabel, 2, 2);
        metricsGrid.add(winningTradesValue, 3, 2);

        // Connect to controller
        controller.netProfitLabel = netProfitValue;
        controller.grossProfitLabel = grossProfitValue;
        controller.grossLossLabel = grossLossValue;
        controller.openPnLLabel = openPnLValue;
        controller.maxDrawdownLabel = maxDrawdownValue;
        controller.maxRunUpLabel = maxRunUpValue;
        controller.sharpeRatioLabel = sharpeRatioValue;
        controller.tradesMadeLabel = tradesMadeValue;
        controller.winningTradesLabel = winningTradesValue;

        section.getChildren().addAll(title, metricsGrid);
        return section;
    }

    private VBox createMonteCarloSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label title = new Label("Monte Carlo Metrics");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Build a table of metric summaries (Mean, Median, P5, P25, P75, P95)
        TableView<MonteCarloMetricRow> table = new TableView<>();
        table.setPrefHeight(200);

        TableColumn<MonteCarloMetricRow, String> metricCol = new TableColumn<>("Metric");
        metricCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("metric"));
        metricCol.setPrefWidth(180);

        TableColumn<MonteCarloMetricRow, String> meanCol = new TableColumn<>("Mean");
        meanCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("mean"));

        TableColumn<MonteCarloMetricRow, String> medianCol = new TableColumn<>("Median");
        medianCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("median"));

        TableColumn<MonteCarloMetricRow, String> p5Col = new TableColumn<>("P5");
        p5Col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("p5"));

        TableColumn<MonteCarloMetricRow, String> p25Col = new TableColumn<>("P25");
        p25Col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("p25"));

        TableColumn<MonteCarloMetricRow, String> p75Col = new TableColumn<>("P75");
        p75Col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("p75"));

        TableColumn<MonteCarloMetricRow, String> p95Col = new TableColumn<>("P95");
        p95Col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("p95"));

        table.getColumns().addAll(metricCol, meanCol, medianCol, p5Col, p25Col, p75Col, p95Col);

        controller.mcTitleLabel = title;
        controller.mcMetricsTable = table;

        section.getChildren().addAll(title, table);
        return section;
    }

    private VBox createMonteCarloPathsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label title = new Label("Monte Carlo Equity Bands");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        VBox pathsContainer = new VBox();
        pathsContainer.setMinHeight(300);
        pathsContainer.setPrefHeight(300);
        pathsContainer.setMaxHeight(300);
        pathsContainer.setStyle("-fx-background-color: transparent;");
        pathsContainer.setAlignment(Pos.CENTER);

        controller.mcPathsContainer = pathsContainer;

        section.getChildren().addAll(title, pathsContainer);
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
        entryDateCol.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
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
                    setStyle("");
                } else {
                    setText(String.format("$%,.2f", item));
                    setStyle("-fx-alignment: center-right;");
                }
            }
        });
        entryPriceCol.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        TableColumn<Trade, String> exitDateCol = new TableColumn<>("Exit Date");
        exitDateCol.setPrefWidth(100);
        exitDateCol.setCellValueFactory(data -> {
            if (data.getValue().isOpen()) {
                return new javafx.beans.property.SimpleStringProperty("");
            } else {
                return new javafx.beans.property.SimpleStringProperty(data.getValue().getExit().get().date.toString());
            }
        });
        exitDateCol.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
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
                    setStyle("");
                } else {
                    setText(String.format("$%,.2f", item));
                    setStyle("-fx-alignment: center-right;");
                }
            }
        });
        exitPriceCol.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        TableColumn<Trade, String> sideCol = new TableColumn<>("Side");
        sideCol.setPrefWidth(80);
        sideCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getDirection().toString()));
        sideCol.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        TableColumn<Trade, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setPrefWidth(100);
        quantityCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        quantityCol.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        TableColumn<Trade, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().isOpen() ? "Open" : "Closed"));
        statusCol.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        TableColumn<Trade, Double> pnlCol = new TableColumn<>("PnL");
        pnlCol.setPrefWidth(100);
        pnlCol.setCellValueFactory(data -> {
            Trade trade = data.getValue();
            if (trade.isOpen()) {
                return new javafx.beans.property.SimpleDoubleProperty(trade.openPnL(controller.lastBar)).asObject();
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
                    setStyle("");
                } else {
                    setText(String.format("$%,.2f", item));
                    // setText(String.format("$%,.2f (0%%)", item)); // TODO: Add percentage calculation
                    if (item > 0) {
                        setStyle("-fx-alignment: center-right; -fx-text-fill: #27ae60;");
                    } else if (item < 0) {
                        setStyle("-fx-alignment: center-right; -fx-text-fill: #e74c3c;");
                    } else {
                        setStyle("-fx-alignment: center-right;");
                    }
                }
            }
        });
        pnlCol.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        tradeTable.getColumns().addAll(entryDateCol, entryPriceCol, exitDateCol, exitPriceCol, sideCol, quantityCol, statusCol, pnlCol);
        
        // Add placeholder
        tradeTable.setPlaceholder(new Label("No trades to display. Run a backtest to see results."));
        
        // Connect to controller
        controller.tradesTable = tradeTable;
        
        section.getChildren().addAll(title, tradeTable);
        return section;
    }
} 
