package HaitamStockProject.objects;


import java.time.LocalDate;

public class SecurityDayValues extends Bar {

    private final int securityId;
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
                             int numTrades)  {
        super(date, open, high, low, close, volume);
        this.securityId = securityId;
        this.vwap = vwap;
        this.numTrades = numTrades;
    }

    public SecurityDayValues withClosePrice(double close) {
        return new SecurityDayValues(securityId, getDate(), getOpen(), getHigh(), getLow(), close, getVolume(), vwap, numTrades);
    }

    public SecurityDayValues withOpenPrice(double open) {
        return new SecurityDayValues(securityId, getDate(), open, getHigh(), getLow(), getClose(), getVolume(), vwap, numTrades);
    }

    public SecurityDayValues withDate(LocalDate date) {
        return new SecurityDayValues(securityId, getDate(), getOpen(), getHigh(), getLow(), getClose(), getVolume(), vwap, numTrades);
    }


    public int getSecurityId() {
        return securityId;
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
                ", date=" + getDate() +
                ", open=" + getOpen() +
                ", high=" + getHigh() +
                ", low=" + getLow() +
                ", close=" + getClose() +
                ", volume=" + getVolume() +
                ", vwap=" + vwap +
                ", numTrades=" + numTrades +
                '}';
    }
}
