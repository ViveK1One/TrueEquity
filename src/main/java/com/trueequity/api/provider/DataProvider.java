package com.trueequity.api.provider;

import com.trueequity.api.dto.StockFundamentalDTO;
import com.trueequity.api.dto.StockInfoDTO;
import com.trueequity.api.dto.StockPriceDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Abstract interface for all data providers
 * Allows easy switching between different APIs
 */
public interface DataProvider {
    
    /**
     * Get basic stock information
     */
    Optional<StockInfoDTO> getStockInfo(String symbol);
    
    /**
     * Get historical daily price data
     */
    List<StockPriceDTO> getHistoricalPrices(String symbol, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get current/latest price
     */
    Optional<BigDecimal> getCurrentPrice(String symbol);
    
    /**
     * Get fundamental data (latest available)
     */
    Optional<StockFundamentalDTO> getFundamentals(String symbol);
    
    /**
     * Get provider name
     */
    String getProviderName();
    
    /**
     * Check if provider is available/healthy
     */
    boolean isAvailable();
}

