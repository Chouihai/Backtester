package Backtester.services;

import Backtester.objects.Bar;

import java.time.LocalDate;
import java.util.List;

public interface HistoricalDataService {

    List<Bar> getHistoricalData(String symbol, LocalDate startDate, LocalDate endDate);

    default int findIndexAfterDate(List<Bar> bars, LocalDate targetDate) {
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
} 
