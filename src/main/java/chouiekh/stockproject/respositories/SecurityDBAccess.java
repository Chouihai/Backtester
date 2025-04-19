package chouiekh.stockproject.respositories;

import chouiekh.stockproject.objects.Security;

import java.util.List;
import java.util.Optional;

public interface SecurityDBAccess {

    Optional<Security> addSecurity(String symbol, String name, String exchange);

    Optional<Security> findSecurityById(int id);

    Optional<Security> findSecurityBySymbol(String symbol);

    List<Security> getAllSecurities();
}

