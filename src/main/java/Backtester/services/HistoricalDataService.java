package Backtester.services;

import Backtester.objects.Bar;

import java.time.LocalDate;
import java.util.List;

public interface HistoricalDataService {

    /**
     * Fetches historical daily data for a security within the specified date range.
     */
    List<Bar> getHistoricalData(String symbol, LocalDate startDate, LocalDate endDate);
} 