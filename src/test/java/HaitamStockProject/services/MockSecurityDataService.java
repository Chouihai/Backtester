package HaitamStockProject.services;

import HaitamStockProject.objects.Security;
import HaitamStockProject.objects.SecurityDayValues;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;

public class MockSecurityDataService implements SecurityDataService {

    HashMap<String, JSONObject> requestsToResponses;


    public MockSecurityDataService(HashMap<String, JSONObject> requestsToResponses) {
        this.requestsToResponses = requestsToResponses;
    }

    @Override
    public JSONObject fetchYTDData(String ticker) {
        return null;
    }

    @Override
    public Security fetchSecurity(String ticker) {
        return null;
    }

    @Override
    public HashMap<LocalDate, SecurityDayValues> fetchSecurityDayValues(String ticker, LocalDate startDate, LocalDate endDate) {
        HashMap<LocalDate, Double> results = new HashMap<>();

        JSONObject json = requestsToResponses.get("AAPL Data");
        if (json == null || (!"OK".equals(json.getString("status")) && !"DELAYED".equals(json.getString("status")))) {
            throw new RuntimeException("Failed to fetch YTD data for " + ticker);
        }
        for (Object obj : json.getJSONArray("results")) {
            JSONObject candle = (JSONObject) obj;
            long timestamp = candle.getLong("t");
            double closePrice = candle.getDouble("c");
            LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();


            if (date.compareTo(startDate) <= 0 && date.compareTo(endDate) >= 0) {
                results.put(date, closePrice);
            }
        }

        return null;
    }

    @Override
    public double fetchOpenPrice(String ticker, String date) {
        return Double.NaN;
    }

}
