package HaitamStockProject.respositories;

import HaitamStockProject.objects.Security;
import HaitamStockProject.services.DatabaseService;
import com.google.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultSecurityDBAccess implements SecurityDBAccess {

    private final DatabaseService databaseService;

    @Inject
    public DefaultSecurityDBAccess(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public Optional<Security> addSecurity(String symbol, String name, String exchange) {
        String sql = "INSERT INTO securities (symbol, name, exchange) VALUES (?, ?, ?) RETURNING id, created_at";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, symbol);
            stmt.setString(2, name);
            stmt.setString(3, exchange);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Security s = new Security(
                        rs.getInt("id"),
                        symbol,
                        name,
                        exchange,
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                return Optional.of(s);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to create security: " + symbol);
            return Optional.empty();
        }
        System.out.println("Failed to create security: " + symbol);
        return Optional.empty();
    }

    public Optional<Security> findSecurityBySymbol(String symbol) {
        String sql = "SELECT * FROM securities WHERE symbol = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, symbol);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Security security = new Security(
                        rs.getInt("id"),
                        rs.getString("symbol"),
                        rs.getString("name"),
                        rs.getString("exchange"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                return Optional.of(security);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<Security> findSecurityById(int id) {
        String sql = "SELECT * FROM securities WHERE id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Security security = new Security(
                        rs.getInt("id"),
                        rs.getString("symbol"),
                        rs.getString("name"),
                        rs.getString("exchange"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                return Optional.of(security);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public List<Security> getAllSecurities() {
        List<Security> securities = new ArrayList<>();
        String sql = "SELECT * FROM securities ORDER BY symbol";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Security security = new Security(
                        rs.getInt("id"),
                        rs.getString("symbol"),
                        rs.getString("name"),
                        rs.getString("exchange"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                securities.add(security);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return securities;
    }
}
