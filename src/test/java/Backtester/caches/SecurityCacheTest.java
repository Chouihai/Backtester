//package Backtester.caches;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import Backtester.objects.Security;
//import Backtester.dbaccess.InMemorySecurityDBAccess;
//import Backtester.dbaccess.SecurityDBAccess;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//import java.util.Optional;
//
//class SecurityCacheTest {
//
//    private InMemorySecurityDBAccess securityDBAccess;
//    private SecurityCache securityCache;
//
//    @BeforeEach
//    void setUp() {
//        securityDBAccess = new InMemorySecurityDBAccess();
//        securityCache = new SecurityCache(securityDBAccess);
//    }
//
//    @Test
//    void testAddSecurity_NewSecurity_ShouldCacheAndReturn() {
//        Optional<Security> security = securityCache.addSecurity("AAPL", "Apple Inc.", "NASDAQ");
//
//        assertTrue(security.isPresent());
//        Optional<Security> cached = securityCache.getById(security.get().getId());
//        assertTrue(cached.isPresent());
//        assertEquals("AAPL", cached.get().getSymbol());
//
//        Optional<Security> cachedBySymbol = securityCache.getBySymbol("AAPL");
//        assertTrue(cachedBySymbol.isPresent());
//        assertEquals(security.get().getId(), cachedBySymbol.get().getId());
//
//        assertTrue(securityDBAccess.findSecurityBySymbol("AAPL").isPresent());
//    }
//
//    @Test
//    void testAddSecurity_AlreadyCached_ShouldReturnSame() {
//        Optional<Security> security1 = securityCache.addSecurity("TSLA", "Tesla Inc.", "NASDAQ");
//        Optional<Security> security2 = securityCache.addSecurity("TSLA", "Tesla Inc.", "NASDAQ");
//
//        assertTrue(security1.isPresent());
//        assertFalse(security2.isPresent());
//    }
//
//    @Test
//    void testGetById_CacheMiss_ShouldLoadFromRepository() {
//        assertFalse(securityCache.getBySymbol("GOOGL").isPresent());
//
//        Optional<Security> security = securityDBAccess.addSecurity("GOOGL", "Alphabet Inc.", "NASDAQ");
//
//        assertTrue(security.isPresent());
//        Optional<Security> found = securityCache.getById(security.get().getId());
//        assertTrue(found.isPresent());
//        assertEquals("GOOGL", found.get().getSymbol());
//        assertEquals(security.get().getId(), found.get().getId());
//    }
//
//    @Test
//    void testGetAllSecurities_ShouldReturnAllCached() {
//        securityCache.addSecurity("NFLX", "Netflix Inc.", "NASDAQ");
//        securityCache.addSecurity("NVDA", "NVIDIA Corp.", "NASDAQ");
//
//        List<Security> securities = securityCache.getAllSecurities();
//
//        assertEquals(2, securities.size());
//        assertTrue(securities.stream().anyMatch(sec -> sec.getSymbol().equals("NFLX")));
//        assertTrue(securities.stream().anyMatch(sec -> sec.getSymbol().equals("NVDA")));
//    }
//
//    @Test
//    void testCachePopulatesOnStartup() {
//        securityDBAccess.addSecurity("AMD", "Advanced Micro Devices", "NASDAQ");
//        securityDBAccess.addSecurity("INTC", "Intel Corporation", "NASDAQ");
//
//        SecurityCache freshCache = new SecurityCache(securityDBAccess);
//
//        List<Security> loaded = freshCache.getAllSecurities();
//        assertEquals(2, loaded.size());
//
//        assertTrue(loaded.stream().anyMatch(s -> s.getSymbol().equals("AMD")));
//        assertTrue(loaded.stream().anyMatch(s -> s.getSymbol().equals("INTC")));
//    }
//
//    @Test
//    void testIfDBAccess_addSecurityFails() {
//        SecurityDBAccess dbAccess = new BadDBAccess();
//        SecurityCache cache = new SecurityCache(dbAccess);
//
//        Optional<Security> security = dbAccess.addSecurity("GOOGL", "Alphabet Inc.", "NASDAQ"); // doesn't work
//        Optional<Security> notFound = cache.getBySymbol("GOOGL");
//        assertFalse(notFound.isPresent());
//    }
//
//    private class BadDBAccess extends InMemorySecurityDBAccess {
//
//        @Override
//        public Optional<Security> addSecurity(String symbol, String name, String exchange) {
//            return Optional.empty();
//        }
//    }
//}
