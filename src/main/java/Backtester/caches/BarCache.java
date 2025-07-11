package Backtester.caches;

import Backtester.objects.Bar;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class BarCache {

    private List<Bar> bars;
    private LocalDate startDate;
    private LocalDate endDate;

    @Inject
    public BarCache() {
        this.bars = new ArrayList<>();
    }

    public void loadCache(List<Bar> bars) {
        this.bars = bars;
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
        if (days > index + 1) {
            throw new RuntimeException("Can't look back further than specified interval");
        }
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
        return -1;
    }

    public int findIndexAfterDate(LocalDate targetDate) {
        if (bars.isEmpty()) {
            return -1;
        }

        // If target date is before the first available date, return 0
        if (targetDate.isBefore(bars.get(0).date)) {
            return 0;
        }

        // If target date is after the last available date, return -1
        if (targetDate.isAfter(bars.get(bars.size() - 1).date)) {
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

        // At this point, left > right, and left points to the first date >= targetDate
        return left;
    }

    public int findIndexBeforeDate(LocalDate targetDate) {
        if (bars.isEmpty()) {
            return -1;
        }

        // If target date is before the first available date, return -1
        if (targetDate.isBefore(bars.get(0).date)) {
            return -1;
        }

        // If target date is after the last available date, return the last index
        if (targetDate.isAfter(bars.get(bars.size() - 1).date)) {
            return bars.size() - 1;
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

        // At this point, left > right, and right points to the last date <= targetDate
        return right;
    }
} 