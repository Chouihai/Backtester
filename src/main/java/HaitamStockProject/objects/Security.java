package HaitamStockProject.objects;

import java.time.LocalDateTime;

public class Security {

    // I think I will make the symbol here the unique identifier, and I will only support US exchange securities.
    // For now I will only support equities and ETFs.
    private final int id;
    private final String symbol;
    private final String name;
    private final String exchange;
    private final LocalDateTime createdAt;

    public Security(int id, String symbol, String name, String exchange, LocalDateTime createdAt) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getExchange() {
        return exchange;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Security{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", exchange='" + exchange + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
