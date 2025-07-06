package Backtester.caches;

import Backtester.objects.Bar;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.LocalDate;
import java.util.*;

// TODO: stop going by dates, and start going by indices
// TODO: should not need a holiday calendar at all

@Singleton
public class BarCache {

    private final List<Bar> bars;
    private LocalDate startDate;
    private LocalDate endDate;

    @Inject
    public BarCache() {
        this.bars = new ArrayList<>();
    }

    public void loadCache(List<Bar> bars) {
        this.bars.addAll(bars);
        startDate = bars.getFirst().date;
        endDate = bars.getLast().date;
    }

    public Bar get(int index) {
        return bars.get(index);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<Bar> getLastNDays(int days, int index) {
        if (days > index + 1) throw new RuntimeException("Can't look back further than specified interval");
        List <Bar> result = new LinkedList<>();
        for (int i = index + 1 - days;  i <= index; i++) {
            result.add(bars.get(i));
        }
        return result;
    }

    public List<Bar> all() {
        return new ArrayList<>(bars);
    }
} 