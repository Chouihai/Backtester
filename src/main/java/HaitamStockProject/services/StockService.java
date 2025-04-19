package HaitamStockProject.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

public class StockService {

    private final String apiKey;

    @Inject
    public StockService(@Named("api.key") String apiKey) {
        this.apiKey = apiKey;
    }

    public Double fetchOpenPrice(String ticker, String date) throws Exception {
        String urlStr = String.format(
                "https://api.polygon.io/v1/open-close/%s/%s?adjusted=true&apiKey=%s",
                ticker, date, apiKey
        );

        JSONObject json = makeApiCall(urlStr);

        if (json == null || (!"OK".equals(json.getString("status")) && !"DELAYED".equals(json.getString("status")))) {
            throw new RuntimeException("Failed to fetch open price for " + ticker);
        }

        return json.getDouble("open");
    }

    public JSONObject fetchYTDData(String ticker) throws Exception {
        LocalDate today = LocalDate.now();
        String from = today.getYear() + "-01-01";
        String to = today.toString();

        String urlStr = String.format(
                "https://api.polygon.io/v2/aggs/ticker/%s/range/1/day/%s/%s?adjusted=true&sort=asc&limit=50000&apiKey=%s",
                ticker, from, to, apiKey
        );

        JSONObject json = makeApiCall(urlStr);

        if (json == null || (!"OK".equals(json.getString("status")) && !"DELAYED".equals(json.getString("status")))) {
            throw new RuntimeException("Failed to fetch YTD data for " + ticker);
        }

        return json;
    }

    private JSONObject makeApiCall(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }
        reader.close();

        return new JSONObject(responseBuilder.toString());
    }
}
