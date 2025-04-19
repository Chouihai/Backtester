package HaitamStockProject.dbaccess;

import HaitamStockProject.objects.SecurityDayValues;
import HaitamStockProject.objects.SecurityDayValuesKey;
import HaitamStockProject.services.DatabaseService;
import com.google.inject.Inject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultSecurityDayValuesDBAccess implements SecurityDayValuesDBAccess {

    private final DatabaseService databaseService;

    @Inject
    public DefaultSecurityDayValuesDBAccess(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void write(SecurityDayValues value) {
        String sql = "INSERT INTO daily_security_values (security_id, date, open, high, low, close, volume, vwap, num_trades) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (security_id, date) DO UPDATE SET " +
                "open = EXCLUDED.open, " +
                "high = EXCLUDED.high, " +
                "low = EXCLUDED.low, " +
                "close = EXCLUDED.close, " +
                "volume = EXCLUDED.volume, " +
                "vwap = EXCLUDED.vwap, " +
                "num_trades = EXCLUDED.num_trades";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value.getSecurityId());
            stmt.setDate(2, Date.valueOf(value.getDate()));
            stmt.setBigDecimal(3, value.getOpen());
            stmt.setBigDecimal(4, value.getHigh());
            stmt.setBigDecimal(5, value.getLow());
            stmt.setBigDecimal(6, value.getClose());
            stmt.setLong(7, value.getVolume());
            stmt.setBigDecimal(8, value.getVwap());
            stmt.setInt(9, value.getNumTrades());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<SecurityDayValues> read(SecurityDayValuesKey valuesKey) {
        String sql = "SELECT * FROM daily_security_values WHERE security_id = ? AND date = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, valuesKey.getSecurityId());
            stmt.setDate(2, Date.valueOf(valuesKey.getDate()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<SecurityDayValues> read(int securityId) {
        List<SecurityDayValues> list = new ArrayList<>();
        String sql = "SELECT * FROM daily_security_values WHERE security_id = ? ORDER BY date";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, securityId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

     public List<SecurityDayValues> getRecentDayValues(int securityId, int days) {
        List<SecurityDayValues> list = new ArrayList<>();
        String sql = "SELECT * FROM daily_security_values WHERE security_id = ? AND date >= CURRENT_DATE - INTERVAL '? days' ORDER BY date";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, securityId);
            stmt.setInt(2, days);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private SecurityDayValues mapResultSet(ResultSet rs) throws SQLException {
        return new SecurityDayValues(
                rs.getInt("security_id"),
                rs.getDate("date").toLocalDate(),
                rs.getBigDecimal("open"),
                rs.getBigDecimal("high"),
                rs.getBigDecimal("low"),
                rs.getBigDecimal("close"),
                rs.getLong("volume"),
                rs.getBigDecimal("vwap"),
                rs.getInt("num_trades")
        );
    }
}
