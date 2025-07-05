package Backtester.caches;

import Backtester.objects.Bar;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BarCache {

    void loadCache(Map<LocalDate, Bar> bars);

    List<Bar> getLastNDays(int days, Bar bar);
} 