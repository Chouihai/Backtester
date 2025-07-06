package Backtester.objects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class Bar {

    public final int index;
    public final LocalDate date;
    public final double open;
    public final double high;
    public final double low;
    public final double close;
    public final long volume;

    public Bar(int index, LocalDate date, double open, double high, double low, double close, long volume) {
        this.index = index;
        this.date = date;
        this.open = BigDecimal.valueOf(open).setScale(2, RoundingMode.HALF_UP).doubleValue();
        this.high = BigDecimal.valueOf(high).setScale(2, RoundingMode.HALF_UP).doubleValue();
        this.low = BigDecimal.valueOf(low).setScale(2, RoundingMode.HALF_UP).doubleValue();
        this.close = BigDecimal.valueOf(close).setScale(2, RoundingMode.HALF_UP).doubleValue();
        this.volume = volume;
    }


    // TODO: figure out the holiday bullshit tomorrow.


    // TODO: when will I need the date? To report when the trades were made.
    //
}
