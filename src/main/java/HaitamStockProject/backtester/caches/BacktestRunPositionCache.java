package HaitamStockProject.backtester.caches;

import HaitamStockProject.objects.Position;

import java.util.Map;

public interface BacktestRunPositionCache {

    void addPosition(Position position);

    Position getPosition(String symbol);

    Map<String, Position> allPositions();
}
