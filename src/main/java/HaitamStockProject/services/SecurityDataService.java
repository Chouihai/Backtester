package HaitamStockProject.services;

import HaitamStockProject.Main;
import HaitamStockProject.caches.SecurityCache;
import HaitamStockProject.caches.SecurityDayValuesCache;
import HaitamStockProject.objects.Security;
import HaitamStockProject.objects.SecurityDayValues;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SecurityDataService {

    private final String apiKey;
    private final SecurityDayValuesCache securityDayValuesCache;
    private final SecurityCache securityCache;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Inject
    public SecurityDataService(@Named("api.key") String apiKey,
                               SecurityDayValuesCache securityDayValuesCache,
                               SecurityCache securityCache) {
        this.securityDayValuesCache = securityDayValuesCache;
        this.apiKey = apiKey;
        this.securityCache = securityCache;
    }

    public BigDecimal fetchOpenPrice(String ticker, String date) throws Exception {
        return fetchSecurityDayValue(ticker, date).getOpen();
    }

    public SecurityDayValues fetchSecurityDayValue(String ticker, String date) throws Exception {
        Optional<Security> securityOpt = securityCache.getBySymbol(ticker);
        Security security;
        if (securityOpt.isPresent()) security = securityOpt.get();
        else security = fetchSecurity(ticker);

        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        Optional<SecurityDayValues> dayValuesOpt = securityDayValuesCache.getDayValues(security.getId(), localDate);

        if (dayValuesOpt.isPresent()) {
            return dayValuesOpt.get();
        }

        String urlStr = String.format(
                "https://api.polygon.io/v2/aggs/ticker/%s/range/1/day/%s/%s?adjusted=true&sort=asc&limit=50000&apiKey=%s",
                ticker, date, date, apiKey
        );

        JSONObject json = makeApiCall(urlStr);

        if (json == null || (!"OK".equals(json.getString("status")) && !"DELAYED".equals(json.getString("status")))) {
            throw new RuntimeException("Failed to fetch day values for " + ticker + " on " + date);
        }

        JSONObject valuesForDay = (JSONObject) json.getJSONArray("results").get(0);

        SecurityDayValues newDayValues = new SecurityDayValues(
                security.getId(),
                localDate,
                BigDecimal.valueOf(valuesForDay.getDouble("o")),
                BigDecimal.valueOf(valuesForDay.getDouble("h")),
                BigDecimal.valueOf(valuesForDay.getDouble("l")),
                BigDecimal.valueOf(valuesForDay.getDouble("c")),
                valuesForDay.getLong("v"),
                BigDecimal.valueOf(valuesForDay.getDouble("vw")),
                valuesForDay.getInt("n")
        );

        securityDayValuesCache.addDayValues(newDayValues);
        return newDayValues;
    }

    public JSONObject fetchYTDData(String ticker) throws Exception {
        LocalDate today = LocalDate.now();
        String from = today.getYear() + "-01-01";
        String to = today.toString();

        String urlStr = String.format(
                "https://api.polygon.io/v2/aggs/ticker/%s/range/1/day/%s/%s?adjusted=true&sort=asc&limit=50000&apiKey=%s",
                ticker, from, to, apiKey
        );

        JSONObject json = makeApiCall(urlStr);

        if (json == null || (!"OK".equals(json.getString("status")) && !"DELAYED".equals(json.getString("status")))) {
            throw new RuntimeException("Failed to fetch YTD data for " + ticker);
        }

        return json;
    }

    /**
     * Fetches security information from Polygon.io and adds it to the SecurityCache.
     */
    public Security fetchSecurity(String ticker) throws Exception {
        logger.info("Fetching security {} from the API", ticker);
        String urlStr = String.format(
                "https://api.polygon.io/v3/reference/tickers/%s?apiKey=%s",
                ticker, apiKey
        );

        JSONObject json = makeApiCall(urlStr);

        if (json == null || !json.has("results")) {
            throw new RuntimeException("Failed to fetch security information for " + ticker);
        }

        JSONObject results = json.getJSONObject("results");

        String symbol = results.getString("ticker");
        String name = results.optString("name", symbol); // fallback if name missing
        String exchange = results.optString("primary_exchange", "UNKNOWN");

        Optional<Security> result = securityCache.addSecurity(symbol, name, exchange);
        if (!result.isPresent()) {
            throw new RuntimeException("Failed to add or retrieve security for ticker " + ticker);
        }
        return result.get();
    }

    private JSONObject makeApiCall(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
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
    }
}
