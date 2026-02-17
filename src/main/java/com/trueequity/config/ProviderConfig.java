package com.trueequity.config;

import com.trueequity.api.provider.AlphaVantageProvider;
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
 * Uses Hybrid provider that combines Yahoo Finance (prices) + Alpha Vantage (fundamentals)
 */
@Configuration
public class ProviderConfig {

    @Autowired
    private YahooFinanceProvider yahooFinanceProvider;
    
    @Autowired
    private AlphaVantageProvider alphaVantageProvider;

    @Value("${app.data-provider.primary:hybrid}")
    private String primaryProvider;

    @Bean
    @Primary
    public DataProvider primaryDataProvider() {
        return switch (primaryProvider.toLowerCase()) {
            case "hybrid" -> new HybridDataProvider(yahooFinanceProvider, alphaVantageProvider);
            case "yahoo_finance", "yahoo" -> yahooFinanceProvider;
            case "alpha_vantage", "alphavantage" -> alphaVantageProvider;
            default -> new HybridDataProvider(yahooFinanceProvider, alphaVantageProvider);
        };
    }
}

