package HaitamStockProject.objects;


import java.time.LocalDate;

public class SecurityDayValues {

    private final int securityId;
    private final LocalDate date;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final long volume;
    private final double vwap;
    private final int numTrades;

    public SecurityDayValues(int securityId,
                             LocalDate date,
                             double open,
                             double high,
                             double low,
                             double close,
                             long volume,
                             double vwap,
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

    public SecurityDayValues withClosePrice(double close) {
        return new SecurityDayValues(securityId, date, open, high, low, close, volume, vwap, numTrades);
    }

    public SecurityDayValues withOpenPrice(double open) {
        return new SecurityDayValues(securityId, date, open, high, low, close, volume, vwap, numTrades);
    }

    public SecurityDayValues withDate(LocalDate date) {
        return new SecurityDayValues(securityId, date, open, high, low, close, volume, vwap, numTrades);
    }


    public int getSecurityId() {
        return securityId;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public long getVolume() {
        return volume;
    }

    public double getVwap() {
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
