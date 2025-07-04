//package HaitamStockProject.dbaccess;
//
//import HaitamStockProject.objects.SecurityDayValues;
//import HaitamStockProject.objects.SecurityDayValuesKey;
//
//import java.time.LocalDate;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//public class InMemorySecurityDayValuesDBAccess implements SecurityDayValuesDBAccess {
//
//    private final Map<SecurityDayValuesKey, SecurityDayValues> storage = new ConcurrentHashMap<>();
//    private final LocalDate today;
//
//    public InMemorySecurityDayValuesDBAccess(LocalDate today) {
//        this.today = today;
//    }
//
//    @Override
//    public void write(SecurityDayValues value) {
//        SecurityDayValuesKey key = new SecurityDayValuesKey(value.getSecurityId(), value.getDate());
//        storage.putIfAbsent(key, value);
//    }
//
//    @Override
//    public Optional<SecurityDayValues> read(SecurityDayValuesKey valuesKey) {
//        return Optional.ofNullable(storage.get(valuesKey));
//    }
//
//    @Override
//    public List<SecurityDayValues> read(int securityId) {
//        List<SecurityDayValues> result = new ArrayList<>();
//        for (SecurityDayValues value : storage.values()) {
//            if (value.getSecurityId() == securityId) {
//                result.add(value);
//            }
//        }
//        result.sort(Comparator.comparing(SecurityDayValues::getDate));
//        return result;
//    }
//
//    @Override
//    public List<SecurityDayValues> getRecentDayValues(int securityId, int days) {
//        LocalDate cutoffDate = today.minusDays(days);
//
//        return storage.values().stream()
//                .filter(val -> val.getSecurityId() == securityId)
//                .filter(val -> !val.getDate().isBefore(cutoffDate))
//                .sorted(Comparator.comparing(SecurityDayValues::getDate))
//                .collect(Collectors.toList());
//    }
//
//}
