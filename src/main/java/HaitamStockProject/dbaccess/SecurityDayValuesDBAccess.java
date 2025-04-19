package HaitamStockProject.dbaccess;

import HaitamStockProject.objects.SecurityDayValues;
import HaitamStockProject.objects.SecurityDayValuesKey;

import java.util.List;
import java.util.Optional;

public interface SecurityDayValuesDBAccess {

    void addDayValues(SecurityDayValues value);

    Optional<SecurityDayValues> getDayValues(SecurityDayValuesKey valuesKey);

    List<SecurityDayValues> getDayValues(int securityId);

    List<SecurityDayValues> getRecentDayValues(int securityId, int days);
}
