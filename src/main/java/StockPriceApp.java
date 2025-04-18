import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class StockPriceApp {

    private static final String API_KEY = "dqHpjXO1Lvm1CI56Vpp8DTcmJi6g8PIX";

    private VBox root;

    public StockPriceApp() {
        setupUI();
    }

    private void setupUI() {
        TextField tickerField = new TextField();
        tickerField.setPromptText("Enter Ticker Symbol (e.g., AAPL)");

        DatePicker datePicker = new DatePicker();

        Button searchButton = new Button("Search");

        Label resultLabel = new Label();

        searchButton.setOnAction(event -> {
            String ticker = tickerField.getText().trim().toUpperCase();
            String date = datePicker.getValue() != null ? datePicker.getValue().toString() : null;

            if (ticker.isEmpty() || date == null) {
                resultLabel.setText("Please enter a ticker and select a date.");
                return;
            }

            String price = fetchOpenPrice(ticker, date);
            resultLabel.setText(price);
        });

        root = new VBox(10, tickerField, datePicker, searchButton, resultLabel);
        root.setPadding(new Insets(20));
    }

    public VBox getRoot() {
        return root;
    }

    private String fetchOpenPrice(String ticker, String date) {
        try {
            String urlStr = String.format(
                    "https://api.polygon.io/v1/open-close/%s/%s?adjusted=true&apiKey=%s",
                    ticker, date, API_KEY
            );
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                return "Failed to fetch data: " + conn.getResponseMessage();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(responseBuilder.toString());
            if (!"OK".equals(json.getString("status"))) {
                return "Error fetching stock data.";
            }

            double openPrice = json.getDouble("open");
            return String.format("Open Price on %s: $%.2f", date, openPrice);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching data.";
        }
    }
}