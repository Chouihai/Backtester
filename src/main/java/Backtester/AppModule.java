package Backtester;

import Backtester.caches.OrderCache;
import Backtester.caches.InMemoryOrderCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.caches.BarCache;
import Backtester.services.DefaultHistoricalDataService;
import Backtester.services.HistoricalDataService;
import Backtester.objects.valueaccumulator.ValueAccumulatorFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
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
        
        // Services
        bind(HistoricalDataService.class).to(DefaultHistoricalDataService.class).in(Singleton.class);
        
        // In-memory caches
        bind(OrderCache.class).to(InMemoryOrderCache.class).in(Singleton.class);
        bind(ValueAccumulatorCache.class).in(Singleton.class);
        bind(BarCache.class).in(Singleton.class);
        
        // Factories
        bind(ValueAccumulatorFactory.class).in(Singleton.class);
    }
}