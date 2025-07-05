package Backtester.services;

import Backtester.objects.Bar;

import java.time.LocalDate;
import java.util.List;

public interface HistoricalDataService {

    /**
     * Fetches historical daily data for a security within the specified date range.
     * Returns cached data if available, otherwise makes a single API call.
     * 
     * @param symbol The security symbol (e.g., "AAPL", "MSFT")
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of Bar objects containing daily OHLCV data
     * @throws RuntimeException if data cannot be fetched
     */
    List<Bar> getHistoricalData(String symbol, LocalDate startDate, LocalDate endDate);
} 