package HaitamStockProject.services;

import HaitamStockProject.objects.Security;
import HaitamStockProject.objects.SecurityDayValues;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

public interface SecurityDataService {

    JSONObject fetchYTDData(String ticker);

    Security fetchSecurity(String ticker);

    HashMap<LocalDate, SecurityDayValues> fetchSecurityDayValues(String ticker, LocalDate startDate, LocalDate endDate);

    double fetchOpenPrice(String ticker, String date);
}
