package HaitamStockProject.caches;

import HaitamStockProject.backtester.caches.PositionCache;
import HaitamStockProject.objects.Position;

import java.util.HashMap;
import java.util.Map;

public class MockPositionCache implements PositionCache {

    private final Map<String, Position> positionMap;

    public MockPositionCache() {
        this.positionMap = new HashMap<>();
    }

    public void addPosition(Position position) {
        this.positionMap.put(position.getSymbol(), position);
    }

    public Position getPosition(String symbol) {
        return positionMap.get(symbol);
    }

    public Map<String, Position> allPositions() {
        return positionMap;
    }
}
