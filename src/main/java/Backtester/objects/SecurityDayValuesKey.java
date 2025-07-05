package Backtester.objects;

import java.time.LocalDate;
import java.util.Objects;

public class SecurityDayValuesKey {

    private final int securityId;
    private final LocalDate date;

    public int getSecurityId() {
        return securityId;
    }

    public LocalDate getDate() {
        return date;
    }

    public SecurityDayValuesKey(int securityId, LocalDate date) {
        this.securityId = securityId;
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecurityDayValuesKey)) return false;
        SecurityDayValuesKey that = (SecurityDayValuesKey) o;
        return securityId == that.securityId && date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(securityId, date);
    }
}
