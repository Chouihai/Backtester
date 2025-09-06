package Backtester.services;

import Backtester.objects.Bar;
import com.google.inject.Inject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DefaultHistoricalDataService implements HistoricalDataService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHistoricalDataService.class);
    private final ConfigurationService configService;

    @Inject
    public DefaultHistoricalDataService(ConfigurationService configService) {
        this.configService = configService;
    }

    // TODO Lot of similarities in this method and file one
    @Override
    public List<Bar> getHistoricalData(String symbol, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching historical data for {} in range {} to {}", symbol, startDate, endDate);
        List<Bar> all = fetchDataFromAlphaVantage(symbol);
        List<Bar> filtered = new ArrayList<>();
        int idx = 0;
        for (Bar b : all) {
            if ((b.date.isEqual(startDate) || b.date.isAfter(startDate)) &&
                (b.date.isEqual(endDate) || b.date.isBefore(endDate))) {
                filtered.add(new Bar(idx++, b.date, b.open, b.high, b.low, b.close, b.volume));
            }
        }
        if (filtered.isEmpty()) {
            throw new RuntimeException("No data available in the selected range for " + symbol);
        }
        return new ArrayList<>(filtered);
    }

    private List<Bar> fetchDataFromAlphaVantage(String symbol) {
        try {
            String apiKey = configService.getApiKey();
            if (!configService.isApiKeyValid()) {
                throw new RuntimeException("API key is not configured. Please set a valid API key in backtester-config.properties");
            }
            
            // Alpha Vantage TIME_SERIES_DAILY endpoint - fetch full available history
            String urlStr = String.format(
                    "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&outputsize=full&apikey=%s",
                    symbol.toUpperCase(),
                    apiKey
            );

            JSONObject response = makeApiCall(urlStr);

            if (response == null) {
                throw new RuntimeException("Failed to fetch data from Alpha Vantage API");
            }

            // Check for API errors
            if (response.has("Error Message")) {
                throw new RuntimeException("Alpha Vantage API Error: " + response.getString("Error Message"));
            }

            if (response.has("Note")) {
                throw new RuntimeException("Alpha Vantage API Rate Limit: " + response.getString("Note"));
            }

            if (!response.has("Time Series (Daily)")) {
                throw new RuntimeException("No data available for " + symbol);
            }

            JSONObject timeSeries = response.getJSONObject("Time Series (Daily)");
            if (timeSeries.length() == 0) {
                throw new RuntimeException("No data available for " + symbol);
            }

            // Parse and create Bar objects
            List<Bar> bars = new ArrayList<>();
            List<String> dates = new ArrayList<>(timeSeries.keySet());
            dates.sort(Comparator.comparing(date -> LocalDate.parse(date, DateTimeFormatter.ISO_DATE)));
            int index = 0;
            for (String dateStr: dates) {
                JSONObject dailyData = timeSeries.getJSONObject(dateStr);
                LocalDate barDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
                Bar bar = parseBarFromJson(index, barDate, dailyData);
                bars.add(bar);
                index++;
            }

            // Return full list; caller filters by date range
            return bars;

        } catch (Exception e) {
            logger.error("Error fetching historical data for {}: {}",
                    symbol, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch historical data: " + e.getMessage(), e);
        }
    }

    private Bar parseBarFromJson(int index, LocalDate date, JSONObject dailyData) {
        double open = Double.parseDouble(dailyData.getString("1. open"));
        double high = Double.parseDouble(dailyData.getString("2. high"));
        double low = Double.parseDouble(dailyData.getString("3. low"));
        double close = Double.parseDouble(dailyData.getString("4. close"));
        long volume = Long.parseLong(dailyData.getString("5. volume"));

        return new Bar(index, date, open, high, low, close, volume);
    }

    private JSONObject makeApiCall(String urlStr) {
        try {
            URI uri = new URI(urlStr);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000); // 10 seconds
            conn.setReadTimeout(30000);    // 30 seconds

            int responseCode = conn.getResponseCode();

            if (responseCode == 429) {
                throw new RuntimeException("API rate limit exceeded. Please wait a moment and try again.");
            }

            if (responseCode == 404) {
                throw new RuntimeException("Symbol not found. Please check the symbol and try again.");
            }

            if (responseCode != 200) {
                throw new RuntimeException("API call failed with response code: " + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            return new JSONObject(responseBuilder.toString());

        } catch (Exception e) {
            logger.error("API call to url {} FAILED!", urlStr, e);
            throw new RuntimeException("Failed to connect to Alpha Vantage API: " + e.getMessage(), e);
        }
    }
}
