package chouiekh.stockproject.repositories;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import chouiekh.stockproject.objects.Security;
import chouiekh.stockproject.respositories.SecurityDBAccess;

public class InMemorySecurityDBAccess implements SecurityDBAccess {

    private final Map<Integer, Security> securitiesById = new ConcurrentHashMap<>();
    private final Map<String, Security> securitiesBySymbol = new ConcurrentHashMap<>();
    private int nextId = 1;

    public Optional<Security> addSecurity(String symbol, String name, String exchange) {
        Security security = new Security(nextId++, symbol, name, exchange, LocalDateTime.now());
        securitiesById.put(security.getId(), security);
        securitiesBySymbol.put(symbol.toUpperCase(), security);
        return Optional.of(security);
    }

    public Optional<Security> findSecurityById(int id) {
        return Optional.ofNullable(securitiesById.get(id));
    }

    public Optional<Security> findSecurityBySymbol(String symbol) {
        return Optional.ofNullable(securitiesBySymbol.get(symbol.toUpperCase()));
    }

    public List<Security> getAllSecurities() {
        return new ArrayList<>(securitiesById.values());
    }
}

