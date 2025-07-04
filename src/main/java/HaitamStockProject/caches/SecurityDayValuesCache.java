//package HaitamStockProject.caches;
//
//import HaitamStockProject.dbaccess.SecurityDayValuesDBAccess;
//import HaitamStockProject.objects.Security;
//import HaitamStockProject.objects.SecurityDayValues;
//import HaitamStockProject.objects.SecurityDayValuesKey;
//import com.google.inject.Inject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.time.LocalDate;
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.List;
//
//public class SecurityDayValuesCache {
//
//    private static final Logger logger = LoggerFactory.getLogger(SecurityDayValuesCache.class);
//    private final SecurityDayValuesDBAccess dbAccess;
//    private final Map<SecurityDayValuesKey, SecurityDayValues> cache = new ConcurrentHashMap<>();
//    private final SecurityCache securityCache;
//
//    @Inject
//    public SecurityDayValuesCache(SecurityDayValuesDBAccess dbAccess,
//                                  SecurityCache securityCache) {
//        this.dbAccess = dbAccess;
//        this.securityCache = securityCache;
//    }
//
//    public void addDayValues(SecurityDayValues value) {
//        SecurityDayValuesKey key = new SecurityDayValuesKey(value.getSecurityId(), value.getDate());
//
//        if (!cache.containsKey(key)) {
//            dbAccess.write(value);
//            cache.put(key, value);
//            logger.debug("Cached daily value for Security ID {} on {}", value.getSecurityId(), value.getDate());
//        } else {
//            logger.debug("Skipped caching duplicate for Security ID {} on {}", value.getSecurityId(), value.getDate());
//        }
//    }
//
//    public Optional<SecurityDayValues> check(int securityId, LocalDate date) {
//        return Optional.ofNullable(cache.get(new SecurityDayValuesKey(securityId, date)));
//    }
//
//    public Optional<SecurityDayValues> getDayValues(int securityId, LocalDate date) {
//        SecurityDayValuesKey key = new SecurityDayValuesKey(securityId, date);
//
//        if (cache.containsKey(key)) {
//            logger.debug("Cache hit for Security ID {} on {}", securityId, date);
//            return Optional.of(cache.get(key));
//        }
//
//        logger.debug("Cache miss for Security ID {} on {}, querying database...", securityId, date);
//        Optional<SecurityDayValues> valueFromDb = dbAccess.read(key);
//        valueFromDb.ifPresent(v -> {
//            cache.put(key, v);
//            logger.debug("Loaded and cached value for Security ID {} on {}", securityId, date);
//        });
//        return valueFromDb;
//    }
//
//    public Optional<SecurityDayValues> getDayValues(String ticker, LocalDate date) {
//        return securityCache.getBySymbol(ticker).flatMap(security -> getDayValues(security.getId(), date));
//    }
//
//
//    public List<SecurityDayValues> getAllDayValues(int securityId) {
//        logger.debug("Fetching all daily values for Security ID {}", securityId);
//        List<SecurityDayValues> values = dbAccess.read(securityId);
//
//        for (SecurityDayValues value : values) {
//            SecurityDayValuesKey key = new SecurityDayValuesKey(value.getSecurityId(), value.getDate());
//            cache.putIfAbsent(key, value);
//        }
//
//        return values;
//    }
//
////    @Timed TODO: add this later
//    public void loadRecentDayValuesIntoCache(int days) {
//        logger.info("Loading last {} days of security day values into cache...", days);
//
//        List<Security> securities = securityCache.getAllSecurities();
//        for (Security security : securities) {
//            List<SecurityDayValues> values = dbAccess.getRecentDayValues(security.getId(), days);
//            for (SecurityDayValues value : values) {
//                SecurityDayValuesKey key = new SecurityDayValuesKey(value.getSecurityId(), value.getDate());
//                cache.put(key, value);
//            }
//        }
//
//        logger.info("Finished loading recent day values into cache for {} securities.", securities.size());
//    }
//
//}
