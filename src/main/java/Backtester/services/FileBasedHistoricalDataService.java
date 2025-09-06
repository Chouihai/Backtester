package Backtester.services;

import Backtester.objects.Bar;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileBasedHistoricalDataService implements HistoricalDataService {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedHistoricalDataService.class);
    private final ConfigurationService configService;

    @Inject
    public FileBasedHistoricalDataService(ConfigurationService configService) {
        this.configService = configService;
    }

    @Override
    public List<Bar> getHistoricalData(String symbol, LocalDate startDate, LocalDate endDate) {
        logger.info("Loading historical data for {} from file in range {} to {}", symbol, startDate, endDate);
        List<Bar> all = loadDataFromFile(symbol);
        List<Bar> filtered = new ArrayList<>();
        int idx = 0;
        for (Bar b: all) {
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

    private List<Bar> loadDataFromFile(String symbol) {
        try {
            String jsonContent = loadJsonFile();
            JSONObject response = new JSONObject(jsonContent);

            List<Bar> bars = new ArrayList<>();
            
            // Check if the JSON has a "results" array (like the test data)
            if (response.has("results") && response.get("results") instanceof JSONArray) {
                bars = parseResultsArray(response.getJSONArray("results"));
            } else {
                // Assume the root object contains date keys with OHLCV data
                bars = parseDateKeyFormat(response);
            }

            if (bars.isEmpty()) {
                throw new RuntimeException("No data available for " + symbol);
            }

            // Sort bars by date
            bars.sort(Comparator.comparing(bar -> bar.date));

            // Return all parsed bars; caller filters
            return bars;

        } catch (Exception e) {
            logger.error("Error loading historical data for {} from file: {}",
                    symbol, e.getMessage(), e);
            throw new RuntimeException("Failed to load historical data from file: " + e.getMessage(), e);
        }
    }

    private List<Bar> parseResultsArray(JSONArray results) {
        List<Bar> bars = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject data = results.getJSONObject(i);
            LocalDate date = parseTimestamp(data);
            Bar bar = parseBarFromJson(i, date, data);
            bars.add(bar);
        }
        return bars;
    }

    private List<Bar> parseDateKeyFormat(JSONObject timeSeries) {
        List<Bar> bars = new ArrayList<>();
        List<String> dates = new ArrayList<>(timeSeries.keySet());
        dates.sort(Comparator.comparing(date -> LocalDate.parse(date, DateTimeFormatter.ISO_DATE)));
        
        for (int i = 0; i < dates.size(); i++) {
            String dateStr = dates.get(i);
            JSONObject dailyData = timeSeries.getJSONObject(dateStr);
            LocalDate barDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            Bar bar = parseBarFromJson(i, barDate, dailyData);
            bars.add(bar);
        }
        return bars;
    }

    private LocalDate parseTimestamp(JSONObject data) {
        if (data.has("t")) {
            // Unix timestamp in milliseconds
            long timestamp = data.getLong("t");
            return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (data.has("timestamp")) {
            // Unix timestamp in milliseconds
            long timestamp = data.getLong("timestamp");
            return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (data.has("date")) {
            // ISO date string
            String dateStr = data.getString("date");
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
        } else {
            throw new RuntimeException("No timestamp field found in data: " + data.toString());
        }
    }

    private String loadJsonFile() throws IOException {
        String filePath = configService.getFilePath();
        logger.info("Loading data from file: {}", filePath);
        
        // First try to load from the configured file path
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            try {
                return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.warn("Could not load from configured path {}, falling back to resources", filePath);
            }
        }
        
        // Fallback to resources
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("AAPL.JSON")) {
            if (inputStream == null) {
                throw new RuntimeException("No data file found. Please check the file path in backtester-config.properties");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Bar parseBarFromJson(int index, LocalDate date, JSONObject dailyData) {
        double open = findDoubleValue(dailyData, "open", "o");
        double high = findDoubleValue(dailyData, "high", "h");
        double low = findDoubleValue(dailyData, "low", "l");
        double close = findDoubleValue(dailyData, "close", "c");
        long volume = findLongValue(dailyData, "volume", "v");

        return new Bar(index, date, open, high, low, close, volume);
    }

    private double findDoubleValue(JSONObject data, String... fieldNames) {
        // First try exact matches
        for (String fieldName : fieldNames) {
            if (data.has(fieldName)) {
                Object value = data.get(fieldName);
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else if (value instanceof String) {
                    return Double.parseDouble((String) value);
                }
            }
        }
        
        // Then try partial matches
        for (String fieldName : fieldNames) {
            for (String key : data.keySet()) {
                if (key.toLowerCase().contains(fieldName.toLowerCase()) || 
                    key.toLowerCase().startsWith(fieldName.toLowerCase())) {
                    Object value = data.get(key);
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    } else if (value instanceof String) {
                        return Double.parseDouble((String) value);
                    }
                }
            }
        }
        
        throw new RuntimeException("No valid field found. Tried: " + String.join(", ", fieldNames));
    }

    private long findLongValue(JSONObject data, String... fieldNames) {
        // First try exact matches
        for (String fieldName : fieldNames) {
            if (data.has(fieldName)) {
                Object value = data.get(fieldName);
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                } else if (value instanceof String) {
                    return Long.parseLong((String) value);
                }
            }
        }
        
        // Then try partial matches
        for (String fieldName : fieldNames) {
            for (String key : data.keySet()) {
                if (key.toLowerCase().contains(fieldName.toLowerCase()) || 
                    key.toLowerCase().startsWith(fieldName.toLowerCase())) {
                    Object value = data.get(key);
                    if (value instanceof Number) {
                        return ((Number) value).longValue();
                    } else if (value instanceof String) {
                        return Long.parseLong((String) value);
                    }
                }
            }
        }
        
        throw new RuntimeException("No valid field found. Tried: " + String.join(", ", fieldNames));
    }
} 
