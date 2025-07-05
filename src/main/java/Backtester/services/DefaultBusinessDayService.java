package Backtester.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class DefaultBusinessDayService implements BusinessDayService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBusinessDayService.class);
    private final String apiKey;
    private final Set<LocalDate> holidays;
    private final Set<LocalDate> businessDays;

    @Inject
    public DefaultBusinessDayService(@Named("api.key") String apiKey) {
        this.apiKey = apiKey;
        this.holidays = new HashSet<>();
        this.businessDays = new HashSet<>();
        initializeHolidays();
    }

    private void initializeHolidays() {
        try {
            // Fetch holidays for the current year
            LocalDate now = LocalDate.now();
            int currentYear = now.getYear();
            
            // Fetch holidays for current year and next year to be safe
            fetchHolidaysForYear(currentYear);
            fetchHolidaysForYear(currentYear + 1);
            
            logger.info("Initialized business day service with {} holidays", holidays.size());
        } catch (Exception e) {
            logger.error("Failed to initialize holidays, using fallback logic", e);
            // Fallback to basic weekend logic if API fails
        }
    }

    private void fetchHolidaysForYear(int year) {
        try {
            String urlStr = String.format(
                "https://api.polygon.io/v1/reference/markets/holidays?year=%d&apiKey=%s",
                year, apiKey
            );

            JSONObject response = makeApiCall(urlStr);
            if (response != null && response.has("results")) {
                JSONArray results = response.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject holiday = results.getJSONObject(i);
                    String dateStr = holiday.getString("date");
                    LocalDate holidayDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
                    holidays.add(holidayDate);
                }
                logger.info("Fetched {} holidays for year {}", results.length(), year);
            }
        } catch (Exception e) {
            logger.error("Failed to fetch holidays for year {}", year, e);
        }
    }

    @Override
    public LocalDate nextBusinessDay(LocalDate date) {
        LocalDate nextDate = date.plusDays(1);
        while (isNotBusinessDay(nextDate)) {
            nextDate = nextDate.plusDays(1);
        }
        return nextDate;
    }

    @Override
    public LocalDate previousBusinessDay(LocalDate date) {
        LocalDate prevDate = date.minusDays(1);
        while (isNotBusinessDay(prevDate)) {
            prevDate = prevDate.minusDays(1);
        }
        return prevDate;
    }

    private boolean isNotBusinessDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY ||
               dow == DayOfWeek.SUNDAY ||
               holidays.contains(date);
    }

    private JSONObject makeApiCall(String urlStr) {
        try {
            URI uri = new URI(urlStr);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                logger.error("API call failed with response code: {}", conn.getResponseCode());
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            return new JSONObject(responseBuilder.toString());
        } catch (Exception e) {
            logger.error("API call to url {} FAILED!", urlStr, e);
            return null;
        }
    }
} 