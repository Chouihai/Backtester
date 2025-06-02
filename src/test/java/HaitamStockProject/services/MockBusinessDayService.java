package HaitamStockProject.services;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public class MockBusinessDayService implements BusinessDayService {

    private Set<LocalDate> holidays;

    public MockBusinessDayService(Set<LocalDate> holidays) {
        this.holidays = holidays;
    }

    @Override
    public LocalDate nextBusinessDay(LocalDate date) {
        LocalDate nextDate = date.plusDays(1);
        while (!isBusinessDay(nextDate)) {
            nextDate = nextDate.plusDays(1);
        }
        return nextDate;
    }

    private boolean isBusinessDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY &&
                dow != DayOfWeek.SUNDAY &&
                !holidays.contains(date);
    }
}
