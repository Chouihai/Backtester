package Backtester.services;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class MockBusinessDayService implements BusinessDayService {

    private Set<LocalDate> holidays;

    public MockBusinessDayService() {
        this.holidays = new HashSet<>();
    }

    public MockBusinessDayService(Set<LocalDate> holidays) {
        this.holidays = holidays;
    }

    public LocalDate nextBusinessDay(LocalDate date) {
        LocalDate nextDate = date.plusDays(1);
        while (isNotBusinessDay(nextDate)) {
            nextDate = nextDate.plusDays(1);
        }
        return nextDate;
    }

    public LocalDate previousBusinessDay(LocalDate date) {
        LocalDate nextDate = date.minusDays(1);
        while (isNotBusinessDay(nextDate)) {
            nextDate = nextDate.minusDays(1);
        }
        return nextDate;
    }

    private boolean isNotBusinessDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY ||
                dow == DayOfWeek.SUNDAY ||
                holidays.contains(date);
    }
}
