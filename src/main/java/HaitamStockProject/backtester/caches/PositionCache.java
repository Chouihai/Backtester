package HaitamStockProject.backtester.caches;

import HaitamStockProject.objects.Position;

import java.util.Map;

/**
 * In memory cache, does not read anything from a database
 */
public interface PositionCache {

    void addPosition(Position position);

    Position getPosition(String symbol);

    Map<String, Position> allPositions();
}
