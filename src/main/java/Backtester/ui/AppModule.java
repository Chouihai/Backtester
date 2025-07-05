package Backtester.ui;

import Backtester.services.BusinessDayService;
import Backtester.services.DefaultBusinessDayService;
import Backtester.caches.BarCache;
import Backtester.caches.InMemoryBarCache;
import Backtester.caches.OrderCache;
import Backtester.caches.InMemoryOrderCache;
import Backtester.caches.ValueAccumulatorCache;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        // Load properties
        Properties props = loadProperties();
        
        // Bind API key
        bind(String.class).annotatedWith(Names.named("api.key"))
            .toInstance(props.getProperty("api.key", ""));
        
        // Core services (in-memory only)
        bind(BusinessDayService.class).to(DefaultBusinessDayService.class).in(Singleton.class);
        
        // In-memory caches
        bind(BarCache.class).to(InMemoryBarCache.class).in(Singleton.class);
        bind(OrderCache.class).to(InMemoryOrderCache.class).in(Singleton.class);
        bind(ValueAccumulatorCache.class).in(Singleton.class);
    }
    
    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            System.err.println("Failed to load application.properties: " + e.getMessage());
        }
        return props;
    }
} 