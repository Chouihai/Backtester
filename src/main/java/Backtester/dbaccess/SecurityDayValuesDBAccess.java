//package Backtester.dbaccess;
//
//import Backtester.objects.SecurityDayValues;
//import Backtester.objects.SecurityDayValuesKey;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface SecurityDayValuesDBAccess {
//
//    void write(SecurityDayValues value);
//
//    Optional<SecurityDayValues> read(SecurityDayValuesKey valuesKey);
//
//    List<SecurityDayValues> read(int securityId);
//
//    List<SecurityDayValues> getRecentDayValues(int securityId, int days);
//}
