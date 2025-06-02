package HaitamStockProject;

import HaitamStockProject.services.SecurityDataService;
import com.google.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockPriceApp {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private VBox root;
    private Label resultLabel;
    private LineChart<String, Number> stockChart;
    private XYChart.Series<String, Number> priceSeries;
    private final SecurityDataService securityDataService;

    @Inject
    public StockPriceApp(SecurityDataService securityDataService) {
        this.securityDataService = securityDataService;
        setupUI();
    }

    public VBox getRoot() {
        return root;
    }

    private void setupUI() {
        TextField tickerField = new TextField();
        tickerField.setPromptText("Enter Ticker Symbol (e.g., AAPL)");

        DatePicker datePicker = new DatePicker();

        Button searchButton = new Button("Search");

        resultLabel = new Label();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Closing Price ($)");

        stockChart = new LineChart<>(xAxis, yAxis);
        stockChart.setTitle("YTD Performance");
        stockChart.setPrefHeight(400);

        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Price");

        stockChart.getData().add(priceSeries);

        searchButton.setOnAction(event -> {
            String ticker = tickerField.getText().trim().toUpperCase();
            String date = datePicker.getValue() != null ? datePicker.getValue().toString() : null;

            if (ticker.isEmpty() || date == null) {
                resultLabel.setText("Please enter a ticker and select a date.");
                return;
            }

            try {
                double openPrice = securityDataService.fetchOpenPrice(ticker, date);
                resultLabel.setText(String.format("Open Price on %s: $%.2f", date, openPrice));

//                HashMap<LocalDate, Double> closePrices = securityDataService.fetchSecurityDayValues(ticker,  LocalDate.of(2024, 1, 1), LocalDate.of(2025, 5, 1));

                JSONObject ytdData = securityDataService.fetchYTDData(ticker);
                updateChartWithYTDData(ytdData);

            } catch (Exception e) {
                e.printStackTrace();
                resultLabel.setText("Error fetching data.");
            }
        });

        root = new VBox(10, tickerField, datePicker, searchButton, resultLabel, stockChart);
        root.setPadding(new Insets(20));
    }

    private void updateChartWithYTDData(JSONObject json) {
        try {
            priceSeries.getData().clear();

            double minPrice = Double.MAX_VALUE;
            double maxPrice = Double.MIN_VALUE;

            for (Object obj : json.getJSONArray("results")) {
                JSONObject candle = (JSONObject) obj;
                long timestamp = candle.getLong("t");
                double closePrice = candle.getDouble("c");

                LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
                String dateStr = date.toString();

                priceSeries.getData().add(new XYChart.Data<>(dateStr, closePrice));

                if (closePrice < minPrice) minPrice = closePrice;
                if (closePrice > maxPrice) maxPrice = closePrice;
            }

            double padding = (maxPrice - minPrice) * 0.05;

            NumberAxis yAxis = (NumberAxis) stockChart.getYAxis();
            yAxis.setLowerBound(minPrice - padding);
            yAxis.setUpperBound(maxPrice + padding);
            yAxis.setAutoRanging(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}