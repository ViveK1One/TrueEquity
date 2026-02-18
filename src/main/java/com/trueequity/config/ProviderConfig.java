package com.trueequity.config;

import com.trueequity.api.provider.FinancialModelingPrepProvider;
import com.trueequity.api.provider.DataProvider;
import com.trueequity.api.provider.HybridDataProvider;
import com.trueequity.api.provider.YahooFinanceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for data providers
 * Uses Hybrid provider that combines Yahoo Finance (prices) + Financial Modeling Prep (fundamentals)
 */
@Configuration
public class ProviderConfig {

    @Autowired
    private YahooFinanceProvider yahooFinanceProvider;
    
    @Autowired
    private FinancialModelingPrepProvider fmpProvider;

    @Value("${app.data-provider.primary:hybrid}")
    private String primaryProvider;

    @Bean
    @Primary
    public DataProvider primaryDataProvider() {
        return switch (primaryProvider.toLowerCase()) {
            case "hybrid" -> new HybridDataProvider(yahooFinanceProvider, fmpProvider);
            case "yahoo_finance", "yahoo" -> yahooFinanceProvider;
            case "fmp", "financial_modeling_prep" -> fmpProvider;
            default -> new HybridDataProvider(yahooFinanceProvider, fmpProvider);
        };
    }
}

