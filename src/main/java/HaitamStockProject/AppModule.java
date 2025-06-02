package HaitamStockProject;

import HaitamStockProject.dbaccess.DefaultSecurityDBAccess;
import HaitamStockProject.dbaccess.DefaultSecurityDayValuesDBAccess;
import HaitamStockProject.dbaccess.SecurityDBAccess;
import HaitamStockProject.dbaccess.SecurityDayValuesDBAccess;
import HaitamStockProject.services.DefaultSecurityDataService;
import HaitamStockProject.services.SecurityDataService;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties not found!");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file", e);
        }

        Names.bindProperties(binder(), properties);
        bind(SecurityDBAccess.class).to(DefaultSecurityDBAccess.class);
        bind(SecurityDayValuesDBAccess.class).to(DefaultSecurityDayValuesDBAccess.class);
        bind(SecurityDataService.class).to(DefaultSecurityDataService.class);
    }
}