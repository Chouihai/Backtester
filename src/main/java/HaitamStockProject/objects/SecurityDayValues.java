package HaitamStockProject.objects;


import java.math.BigDecimal;
import java.time.LocalDate;

public class SecurityDayValues {

    private final int securityId;
    private final LocalDate date;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final long volume;
    private final BigDecimal vwap;
    private final int numTrades;

    public SecurityDayValues(int securityId,
                           LocalDate date,
                           BigDecimal open,
                           BigDecimal high,
                           BigDecimal low,
                           BigDecimal close,
                           long volume,
                           BigDecimal vwap,
                           int numTrades) {
        this.securityId = securityId;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.vwap = vwap;
        this.numTrades = numTrades;
    }

    public int getSecurityId() {
        return securityId;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public long getVolume() {
        return volume;
    }

    public BigDecimal getVwap() {
        return vwap;
    }

    public int getNumTrades() {
        return numTrades;
    }

    @Override
    public String toString() {
        return "DailyStockValue{" +
                ", securityId=" + securityId +
                ", date=" + date +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                ", vwap=" + vwap +
                ", numTrades=" + numTrades +
                '}';
    }
}
