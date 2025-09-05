package Backtester.services;

import com.google.inject.Inject;

public class HistoricalDataServiceFactory {
    
    private final ConfigurationService configService;
    
    @Inject
    public HistoricalDataServiceFactory(ConfigurationService configService) {
        this.configService = configService;
    }
    
    public HistoricalDataService createService() {
        ConfigurationService.DataSource dataSource = configService.getDataSource();
        
        switch (dataSource) {
            case API:
                return new DefaultHistoricalDataService(configService);
            case FILE:
                return new FileBasedHistoricalDataService(configService);
            default:
                throw new IllegalStateException("Unknown data source: " + dataSource);
        }
    }
} 