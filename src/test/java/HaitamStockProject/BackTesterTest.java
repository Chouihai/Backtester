package HaitamStockProject;

import HaitamStockProject.backtester.BackTester;
import HaitamStockProject.backtester.BacktesterResult;
import HaitamStockProject.services.BusinessDayService;
import HaitamStockProject.services.MockBusinessDayService;
import HaitamStockProject.services.MockSecurityDataService;
import HaitamStockProject.services.SecurityDataService;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BackTesterTest {


    private SecurityDataService securityDataService;
    private JSONObject aaplData;
    private HashMap<String, JSONObject> requestsToAnswers = new HashMap<>();
    private BusinessDayService businessDayService = new MockBusinessDayService(new HashSet<>());
    private BackTester backTester;

    public BackTesterTest() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/test/java/AAPL.JSON")));
            this.aaplData = new JSONObject(content);
            this.requestsToAnswers.put("AAPL DATA", this.aaplData);
            this.securityDataService = new MockSecurityDataService(this.requestsToAnswers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void foo() {
        backTester = new BackTester(100_000.0,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2025, 5, 1),
                null,
                "AAPL",
                securityDataService,
                businessDayService);
        BacktesterResult result = backTester.run();
        assertEquals(result.getFinalBalance(), 96_600.0);
        assertEquals(result.getTrades().size(), 10);
    }

}
