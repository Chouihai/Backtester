package Backtester.services;

import Backtester.objects.Bar;
import Backtester.caches.BarCache;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PolygonHistoricalDataService implements HistoricalDataService {

    private static final Logger logger = LoggerFactory.getLogger(PolygonHistoricalDataService.class);
    private final String apiKey;
    private final BarCache barCache;

    @Inject
    public PolygonHistoricalDataService(@Named("api.key") String apiKey, BarCache barCache) {
        this.apiKey = apiKey;
        this.barCache = barCache;
    }

    @Override
    public List<Bar> getHistoricalData(String symbol, LocalDate startDate, LocalDate endDate) {
        // TODO: Cache API data later, for now just going straight ahead and loading everything in the cache.
//        Map<LocalDate, Bar> cachedBars = barCache.getBars();
//        if (!cachedBars.isEmpty()) {
//            logger.info("Using cached data for {} from {} to {}", symbol, startDate, endDate);
//            return filterBarsByDateRange(cachedBars, startDate, endDate);
//        }

        // Fetch data from API
        logger.info("Fetching historical data for {} from {} to {}", symbol, startDate, endDate);
        return fetchDataFromPolygon(symbol, startDate, endDate);
    }

    private String createCacheKey(String symbol, LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s_%s", symbol.toUpperCase(), startDate, endDate);
    }

    private List<Bar> filterBarsByDateRange(Map<LocalDate, Bar> bars, LocalDate startDate, LocalDate endDate) {
        List<Bar> filteredBars = new ArrayList<>();
        for (Map.Entry<LocalDate, Bar> entry : bars.entrySet()) {
            LocalDate barDate = entry.getKey();
            if (!barDate.isBefore(startDate) && !barDate.isAfter(endDate)) {
                filteredBars.add(entry.getValue());
            }
        }
        return filteredBars;
    }

    private List<Bar> fetchDataFromPolygon(String symbol, LocalDate startDate, LocalDate endDate) {
        try {
            String urlStr = String.format(
                "https://api.polygon.io/v2/aggs/ticker/%s/range/1/day/%s/%s?adjusted=true&sort=asc&apiKey=%s",
                symbol.toUpperCase(),
                startDate.format(DateTimeFormatter.ISO_DATE),
                endDate.format(DateTimeFormatter.ISO_DATE),
                apiKey
            );

            JSONObject response = makeApiCall(urlStr);
            
            if (response == null) {
                throw new RuntimeException("Failed to fetch data from Polygon.io API");
            }

            if (!response.has("results")) {
                throw new RuntimeException("No data available for " + symbol + " from " + startDate + " to " + endDate);
            }

            JSONArray results = response.getJSONArray("results");
            if (results.length() == 0) {
                throw new RuntimeException("No data available for " + symbol + " from " + startDate + " to " + endDate);
            }

            // Parse and create Bar objects
            Map<LocalDate, Bar> barsMap = new TreeMap<>();
            for (int i = 0; i < results.length(); i++) {
                JSONObject candle = results.getJSONObject(i);
                Bar bar = parseBarFromJson(candle);
                barsMap.put(bar.date, bar);
            }

            // Load data into cache
            barCache.loadCache(barsMap);
            
            logger.info("Successfully fetched {} bars for {} from {} to {}", 
                barsMap.size(), symbol, startDate, endDate);

            return new ArrayList<>(barsMap.values());

        } catch (Exception e) {
            logger.error("Error fetching historical data for {} from {} to {}: {}", 
                symbol, startDate, endDate, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch historical data: " + e.getMessage(), e);
        }
    }

    private Bar parseBarFromJson(JSONObject candle) {
        long timestamp = candle.getLong("t");
        LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        
        double open = candle.getDouble("o");
        double high = candle.getDouble("h");
        double low = candle.getDouble("l");
        double close = candle.getDouble("c");
        long volume = candle.getLong("v");

        return new Bar(date, open, high, low, close, volume);
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
            throw new RuntimeException("Failed to connect to Polygon.io API: " + e.getMessage(), e);
        }
    }
} 