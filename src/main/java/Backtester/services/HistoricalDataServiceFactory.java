package Backtester.services;

import Backtester.caches.BarCache;
import com.google.inject.Inject;

public class HistoricalDataServiceFactory {
    
    private final ConfigurationService configService;
    private final BarCache barCache;
    
    @Inject
    public HistoricalDataServiceFactory(ConfigurationService configService, BarCache barCache) {
        this.configService = configService;
        this.barCache = barCache;
    }
    
    public HistoricalDataService createService() {
        ConfigurationService.DataSource dataSource = configService.getDataSource();
        
        switch (dataSource) {
            case API:
                return new DefaultHistoricalDataService(barCache, configService);
            case FILE:
                return new FileBasedHistoricalDataService(barCache, configService);
            default:
                throw new IllegalStateException("Unknown data source: " + dataSource);
        }
    }
} 