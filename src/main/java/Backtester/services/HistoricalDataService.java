package Backtester.services;

import Backtester.objects.Bar;

import java.time.LocalDate;
import java.util.List;

public interface HistoricalDataService {

    List<Bar> getHistoricalData(String symbol, LocalDate startDate, LocalDate endDate);
} 
