package HaitamStockProject;

public class Stopwatch {

    private final long startTime;

    private Stopwatch() {
        this.startTime = System.nanoTime();
    }

    public static Stopwatch start() {
        return new Stopwatch();
    }

    public long elapsedMillis() {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}