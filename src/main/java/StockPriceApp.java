import Services.StockService;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;

import org.json.JSONObject;

public class StockPriceApp {

    private VBox root;
    private Label resultLabel;
    private LineChart<String, Number> stockChart;
    private XYChart.Series<String, Number> priceSeries;
    private final StockService stockService;

    @Inject
    public StockPriceApp(StockService stockService) {
        this.stockService = stockService;
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
                Double openPrice = stockService.fetchOpenPrice(ticker, date);
                resultLabel.setText(String.format("Open Price on %s: $%.2f", date, openPrice));

                JSONObject ytdData = stockService.fetchYTDData(ticker);
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


//    private String fetchOpenPrice(String ticker, String date) {
//        try {
//            String urlStr = String.format(
//                    "https://api.polygon.io/v1/open-close/%s/%s?adjusted=true&apiKey=%s",
//                    ticker, date, API_KEY
//            );
//            URL url = new URL(urlStr);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//
//            if (conn.getResponseCode() != 200) {
//                return "Failed to fetch data: " + conn.getResponseMessage();
//            }
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            StringBuilder responseBuilder = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                responseBuilder.append(line);
//            }
//            reader.close();
//
//            JSONObject json = new JSONObject(responseBuilder.toString());
//            if (!"OK".equals(json.getString("status"))) {
//                return "Error fetching stock data.";
//            }
//
//            double openPrice = json.getDouble("open");
//            return String.format("Open Price on %s: $%.2f", date, openPrice);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Error fetching data.";
//        }
//    }

//    private void fetchAndDisplayYTDData(String ticker) {
//        try {
//            LocalDate today = LocalDate.now();
//            String from = today.getYear() + "-01-01";
//            String to = today.toString();
//
//            String urlStr = String.format(
//                    "https://api.polygon.io/v2/aggs/ticker/%s/range/1/day/%s/%s?adjusted=true&sort=asc&limit=50000&apiKey=%s",
//                    ticker, from, to, API_KEY
//            );
//            URL url = new URL(urlStr);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//
//            if (conn.getResponseCode() != 200) {
//                resultLabel.setText("Failed to fetch YTD data: " + conn.getResponseMessage());
//                return;
//            }
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            StringBuilder responseBuilder = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                responseBuilder.append(line);
//            }
//            reader.close();
//
//            JSONObject json = new JSONObject(responseBuilder.toString());
//            String status = json.getString("status");
//            if (!"OK".equals(status) && !"DELAYED".equals(status)) {
//                resultLabel.setText("Error fetching YTD stock data. Received status: " + json.getString("status"));
//                return;
//            }
//
//            priceSeries.getData().clear();
//
//            double minPrice = Double.MAX_VALUE;
//            double maxPrice = Double.MIN_VALUE;
//
//            for (Object obj : json.getJSONArray("results")) {
//                JSONObject candle = (JSONObject) obj;
//                long timestamp = candle.getLong("t");
//                double closePrice = candle.getDouble("c");
//
//                LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
//                String dateStr = date.toString();
//
//                priceSeries.getData().add(new XYChart.Data<>(dateStr, closePrice));
//
//                if (closePrice < minPrice) minPrice = closePrice;
//                if (closePrice > maxPrice) maxPrice = closePrice;
//            }
//
//            double padding = (maxPrice - minPrice) * 0.05;
//
//            ((NumberAxis) stockChart.getYAxis()).setLowerBound(minPrice - padding);
//            ((NumberAxis) stockChart.getYAxis()).setUpperBound(maxPrice + padding);
//            ((NumberAxis) stockChart.getYAxis()).setAutoRanging(false); // Important: disable auto-ranging!
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            resultLabel.setText("Error fetching YTD data: " + e.toString());
//        }
//    }

}