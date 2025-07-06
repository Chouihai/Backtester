package Backtester.ui;

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
        
        // In-memory caches
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