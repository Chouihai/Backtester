package HaitamStockProject.services;

import java.time.LocalDate;

public interface BusinessDayService {

    LocalDate nextBusinessDay(LocalDate date);
}
