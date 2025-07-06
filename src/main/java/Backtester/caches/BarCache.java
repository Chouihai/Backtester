package Backtester.caches;

import Backtester.objects.Bar;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.LocalDate;
import java.util.*;

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

    public Bar getBarByDate(LocalDate date) {
        int index = findIndexByDate(date);
        if (index >= 0 && index < bars.size()) {
            return bars.get(index);
        }
        return null;
    }

    /**
     * Because bars are sorted chronologically, we can do a binary search TODO: unit test this
     */
    public int findIndexByDate(LocalDate targetDate) {
        if (bars.isEmpty()) {
            return -1;
        }

        int left = 0;
        int right = bars.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            LocalDate midDate = bars.get(mid).date;

            int comparison = midDate.compareTo(targetDate);

            if (comparison == 0) {
                return mid;
            } else if (comparison < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        if (left < bars.size()) {
            return left;
        }

        return -1;
    }

} 