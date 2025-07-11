package Backtester.services;

import Backtester.caches.BarCache;
import Backtester.objects.Bar;
import com.google.inject.Inject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileBasedHistoricalDataService implements HistoricalDataService {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedHistoricalDataService.class);
    private final BarCache barCache;
    private final ConfigurationService configService;

    @Inject
    public FileBasedHistoricalDataService(BarCache barCache, ConfigurationService configService) {
        this.barCache = barCache;
        this.configService = configService;
    }

    @Override
    public List<Bar> getHistoricalData(String symbol) {
        logger.info("Loading historical data for {} from file", symbol);
        return loadDataFromFile(symbol);
    }

    private List<Bar> loadDataFromFile(String symbol) {
        try {
            // Load the JSON file from resources
            String jsonContent = loadJsonFile();
            JSONObject response = new JSONObject(jsonContent);

            // The file contains the time series data directly
            JSONObject timeSeries = response;

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

            // Load data into cache
            barCache.loadCache(bars);

            String filePath = configService.getFilePath();
            logger.info("Successfully loaded {} bars for {} from file: {}",
                    bars.size(), symbol, filePath);

            return new ArrayList<>(bars);

        } catch (Exception e) {
            logger.error("Error loading historical data for {} from file: {}",
                    symbol, e.getMessage(), e);
            throw new RuntimeException("Failed to load historical data from file: " + e.getMessage(), e);
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
        double open = Double.parseDouble(dailyData.getString("1. open"));
        double high = Double.parseDouble(dailyData.getString("2. high"));
        double low = Double.parseDouble(dailyData.getString("3. low"));
        double close = Double.parseDouble(dailyData.getString("4. close"));
        long volume = Long.parseLong(dailyData.getString("5. volume"));

        return new Bar(index, date, open, high, low, close, volume);
    }
} 