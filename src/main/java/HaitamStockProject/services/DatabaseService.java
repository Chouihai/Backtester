package HaitamStockProject.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseService {

    private final String url;
    private final String username;
    private final String password;

    @Inject
    public DatabaseService(
            @Named("db.url") String url,
            @Named("db.username") String username,
            @Named("db.password") String password
    ) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
