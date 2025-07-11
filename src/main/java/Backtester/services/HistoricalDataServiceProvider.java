package Backtester.services;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class HistoricalDataServiceProvider implements Provider<HistoricalDataService> {
    
    private final HistoricalDataServiceFactory factory;
    
    @Inject
    public HistoricalDataServiceProvider(HistoricalDataServiceFactory factory) {
        this.factory = factory;
    }
    
    @Override
    public HistoricalDataService get() {
        return factory.createService();
    }
} 