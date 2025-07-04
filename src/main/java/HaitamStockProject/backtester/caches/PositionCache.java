package HaitamStockProject.backtester.caches;

import HaitamStockProject.objects.Position;

import java.util.Map;

/**
 * In memory cache, does not read anything from a database
 */
public interface PositionCache { // Not needed atm, we only track one position per backtesting run

    void addPosition(Position position);

    Position getPosition(String symbol);

    Map<String, Position> allPositions();
}
