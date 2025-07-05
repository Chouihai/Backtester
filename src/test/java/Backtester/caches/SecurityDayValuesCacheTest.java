//package Backtester.caches;
//
//import Backtester.dbaccess.InMemorySecurityDBAccess;
//import Backtester.dbaccess.InMemorySecurityDayValuesDBAccess;
//import Backtester.objects.Security;
//import Backtester.objects.SecurityDayValues;
//import Backtester.objects.SecurityDayValuesKey;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class SecurityDayValuesCacheTest {
//
//    private SecurityDayValuesCache cache;
//    private InMemorySecurityDayValuesDBAccess inMemoryDb;
//    private SecurityCache securityCache;
//    private InMemorySecurityDBAccess inMemorySecurityDb;
//
//
//    private final LocalDate march1st = LocalDate.of(2025, 3, 1);
//
//    @BeforeEach
//    void setUp() {
//        inMemorySecurityDb = new InMemorySecurityDBAccess();
//        securityCache = new SecurityCache(inMemorySecurityDb);
//        inMemoryDb = new InMemorySecurityDayValuesDBAccess(march1st);
//        cache = new SecurityDayValuesCache(inMemoryDb, securityCache);
//    }
//
//    private SecurityDayValues createSampleDayValue(int securityId, LocalDate date) {
//        return new SecurityDayValues(
//                securityId,
//                date,
//                100,
//                110,
//                90,
//                105,
//                1_000_000,
//                102,
//                5000
//        );
//    }
//
//    @Test
//    void testAddDayValues_CachesAndPersists() {
//        SecurityDayValues dayValue = createSampleDayValue(1, march1st);
//
//        cache.addDayValues(dayValue);
//
//        Optional<SecurityDayValues> fromCache = cache.getDayValues(1, march1st);
//        assertTrue(fromCache.isPresent());
//        assertEquals(BigDecimal.valueOf(100), fromCache.get().getOpen());
//
//        Optional<SecurityDayValues> fromDb = inMemoryDb.read(new SecurityDayValuesKey(1, march1st));
//        assertTrue(fromDb.isPresent());
//        SecurityDayValues values = fromDb.get();
//        assertEquals(1, values.getSecurityId());
//        assertEquals(march1st, values.getDate());
//        assertEquals(BigDecimal.valueOf(100), values.getOpen());
//        assertEquals(BigDecimal.valueOf(100), values.getOpen());
//        assertEquals(BigDecimal.valueOf(110), values.getHigh());
//        assertEquals(BigDecimal.valueOf(90), values.getLow());
//        assertEquals(BigDecimal.valueOf(105), values.getClose());
//        assertEquals(1_000_000, values.getVolume());
//        assertEquals(BigDecimal.valueOf(102), values.getVwap());
//        assertEquals(5000, values.getNumTrades());
//    }
//
//    @Test
//    void testGetDayValues_CacheMissLoadsFromDb() {
//        SecurityDayValues dayValue = createSampleDayValue(2, march1st);
//        inMemoryDb.write(dayValue);
//
//        Optional<SecurityDayValues> result = cache.getDayValues(2, march1st);
//
//        assertTrue(result.isPresent());
//        assertEquals(BigDecimal.valueOf(100), result.get().getOpen());
//    }
//
//    @Test
//    void testGetAllDayValues_ReturnsCorrectly() {
//        inMemoryDb.write(createSampleDayValue(3, march1st.minusDays(1)));
//        inMemoryDb.write(createSampleDayValue(3, march1st));
//
//        List<SecurityDayValues> allValues = cache.getAllDayValues(3);
//
//        assertEquals(2, allValues.size());
//        assertEquals(march1st.minusDays(1), allValues.get(0).getDate());
//        assertEquals(march1st, allValues.get(1).getDate());
//    }
//
//    @Test
//    void testLoadRecentDayValuesIntoCache() {
//        securityCache.addSecurity("AAPL", "Apple Inc.", "NYSE");
//        Security aapl = securityCache.getBySymbol("AAPL").get();
//        int aaplId = aapl.getId();
//        inMemoryDb.write(createSampleDayValue(aaplId, march1st.minusDays(10)));
//        inMemoryDb.write(createSampleDayValue(aaplId, march1st.minusDays(20)));
//        inMemoryDb.write(createSampleDayValue(aaplId, march1st.minusDays(40)));
//
//        cache.loadRecentDayValuesIntoCache(30);
//
//        Optional<SecurityDayValues> val10 = cache.check(aaplId, march1st.minusDays(10));
//        Optional<SecurityDayValues> val20 = cache.check(aaplId, march1st.minusDays(20));
//        Optional<SecurityDayValues> val40 = cache.check(aaplId, march1st.minusDays(40));
//
//        assertTrue(val10.isPresent());
//        assertTrue(val20.isPresent());
//        assertFalse(val40.isPresent());
//    }
//}
//
