package chouiekh.stockproject.caches;

import chouiekh.stockproject.objects.Security;
import chouiekh.stockproject.respositories.SecurityDBAccess;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SecurityCache {

    private final Map<Integer, Security> securitiesById = new ConcurrentHashMap<>();
    private final Map<String, Security> securitiesBySymbol = new ConcurrentHashMap<>();
    private final SecurityDBAccess securityDBAccess;

    @Inject
    public SecurityCache(SecurityDBAccess securityDBAccess) {
        this.securityDBAccess = securityDBAccess;
        loadAllSecuritiesIntoCache();
    }

    private void loadAllSecuritiesIntoCache() {
        List<Security> securities = securityDBAccess.getAllSecurities();
        for (Security security : securities) {
            this.securitiesById.put(security.getId(), security);
            this.securitiesBySymbol.put(security.getSymbol(), security);
        }
    }

    public Optional<Security> getById(Integer id) {
        if (securitiesById.containsKey(id)) {
            return Optional.of(securitiesById.get(id));
        }

        // Not in cache, try DB
        Optional<Security> securityFromDb = securityDBAccess.findSecurityById(id);
        securityFromDb.ifPresent(security -> {
            securitiesById.put(security.getId(), security);
            securitiesBySymbol.put(security.getSymbol(), security);
        });
        return securityFromDb;
    }

    public Optional<Security> getBySymbol(String symbol) {
        if (securitiesBySymbol.containsKey(symbol)) {
            return Optional.of(securitiesBySymbol.get(symbol));
        }

        // Not in cache, try DB
        Optional<Security> securityFromDb = securityDBAccess.findSecurityBySymbol(symbol);
        securityFromDb.ifPresent(security -> {
            securitiesById.put(security.getId(), security);
            securitiesBySymbol.put(security.getSymbol(), security);
        });
        return securityFromDb;
    }

    /**
     * Returns the security if added. Returns nothing if the security was not added (already in the cache)
     */
    public Optional<Security> addSecurity(String symbol, String name, String exchange) {

        if (securitiesBySymbol.containsKey(symbol)) {
            return Optional.empty();
        }

        Optional<Security> optionalSecurity = securityDBAccess.addSecurity(symbol, name, exchange);
        optionalSecurity.ifPresent(security -> {
            securitiesById.put(security.getId(), security);
            securitiesBySymbol.put(security.getSymbol(), security);
        });
        return optionalSecurity;
    }

    public List<Security> getAllSecurities() {
        return new ArrayList<>(securitiesById.values());
    }
}
