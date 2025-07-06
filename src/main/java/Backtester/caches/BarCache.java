package Backtester.caches;

import Backtester.objects.Bar;
import Backtester.services.BusinessDayService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.LocalDate;
import java.util.*;

@Singleton
public class BarCache {

    private final Map<LocalDate, Bar> bars;
    private final BusinessDayService businessDayService;

    @Inject
    public BarCache(BusinessDayService businessDayService) {
        this.bars = new TreeMap<>();
        this.businessDayService = businessDayService;
    }

    public void loadCache(Map<LocalDate, Bar> bars) {
        this.bars.putAll(bars);
    }

    public Bar get(LocalDate date) {
        return bars.get(date);
    }

    public List<Bar> getLastNDays(int days, Bar bar) {
        List <Bar> result = new LinkedList<>();
        LocalDate currentDate = businessDayService.previousBusinessDay(bar.date);
        for (int i = days;  i > 0; i--) {
            Bar b = this.bars.get(currentDate);
            if (b == null) throw new RuntimeException("Could not find bar for date " + currentDate);
            result.addFirst(b);
            currentDate = businessDayService.previousBusinessDay(currentDate);
        }
        return result;
    }

    public Map<LocalDate, Bar> getBars() {
        return new HashMap<>(bars);
    }

    public List<Bar> getBars(LocalDate startDate, LocalDate endDate) {
        List<Bar> result = new LinkedList<>();
        LocalDate currentDate = startDate;
        while (currentDate.isBefore(endDate) || currentDate.equals(endDate)) {
            Bar currentBar = bars.get(currentDate);
            if (currentBar == null) throw new RuntimeException("Did not find bar for valid date: " + currentDate);
            result.add(currentBar);
            currentDate = businessDayService.nextBusinessDay(currentDate);
        }
        return result;
    }
} 