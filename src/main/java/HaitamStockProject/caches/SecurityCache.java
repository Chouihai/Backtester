package HaitamStockProject.caches;

import HaitamStockProject.objects.Security;
import HaitamStockProject.respositories.SecurityRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SecurityCache {

    private final Map<Integer, Security> securitiesById = new ConcurrentHashMap<>();
    private final Map<String, Security> securitiesBySymbol = new ConcurrentHashMap<>();
    private final SecurityRepository securityRepository;

    @Inject
    public SecurityCache(SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
        loadAllSecuritiesIntoCache();
    }

    private void loadAllSecuritiesIntoCache() {
        List<Security> securities = securityRepository.getAllSecurities();
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
        Optional<Security> securityFromDb = securityRepository.findSecurityById(id);
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
        Optional<Security> securityFromDb = securityRepository.findSecurityBySymbol(symbol);
        securityFromDb.ifPresent(security -> {
            securitiesById.put(security.getId(), security);
            securitiesBySymbol.put(security.getSymbol(), security);
        });
        return securityFromDb;
    }


    public Security addSecurity(String symbol, String name, String exchange) {

        if (securitiesBySymbol.containsKey(symbol)) {
            return securitiesBySymbol.get(symbol);
        }

        Security newSecurity = securityRepository.addSecurity(symbol, name, exchange);
        securitiesById.put(newSecurity.getId(), newSecurity);
        securitiesBySymbol.put(newSecurity.getSymbol(), newSecurity);
        return newSecurity;
    }

    public List<Security> getAllSecurities() {
        return new ArrayList<>(securitiesById.values());
    }
}
