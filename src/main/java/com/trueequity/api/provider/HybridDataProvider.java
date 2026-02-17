package com.trueequity.api.provider;

import com.trueequity.api.dto.StockFundamentalDTO;
import com.trueequity.api.dto.StockInfoDTO;
import com.trueequity.api.dto.StockPriceDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Hybrid data provider that uses:
 * - Yahoo Finance for prices and basic info (fast, no rate limits)
 * - Alpha Vantage for comprehensive financial data (reliable fundamentals)
 */
@Component
public class HybridDataProvider implements DataProvider {

    private final YahooFinanceProvider yahooFinanceProvider;
    private final AlphaVantageProvider alphaVantageProvider;

    public HybridDataProvider(YahooFinanceProvider yahooFinanceProvider, 
                             AlphaVantageProvider alphaVantageProvider) {
        this.yahooFinanceProvider = yahooFinanceProvider;
        this.alphaVantageProvider = alphaVantageProvider;
    }

    @Override
    public Optional<StockInfoDTO> getStockInfo(String symbol) {
        // Use Alpha Vantage as primary source for sector, industry, market cap
        // Use Yahoo Finance only for name and exchange if Alpha Vantage doesn't have them
        Optional<StockInfoDTO> alphaInfo = alphaVantageProvider.getStockInfo(symbol);
        
        if (alphaInfo.isPresent()) {
            StockInfoDTO info = alphaInfo.get();
            
            // If Alpha Vantage has name and exchange, use it directly
            if (info.getName() != null && !info.getName().isEmpty() && 
                info.getExchange() != null && !info.getExchange().isEmpty()) {
                return Optional.of(info);
            }
            
            // If Alpha Vantage doesn't have name/exchange, try Yahoo Finance
            Optional<StockInfoDTO> yahooInfo = yahooFinanceProvider.getStockInfo(symbol);
            if (yahooInfo.isPresent()) {
                StockInfoDTO yahoo = yahooInfo.get();
                // Fill in name and exchange from Yahoo, keep sector/industry/market cap from Alpha Vantage
                if ((info.getName() == null || info.getName().isEmpty()) && 
                    yahoo.getName() != null && !yahoo.getName().isEmpty()) {
                    info.setName(yahoo.getName());
                }
                if ((info.getExchange() == null || info.getExchange().isEmpty()) && 
                    yahoo.getExchange() != null && !yahoo.getExchange().isEmpty()) {
                    info.setExchange(yahoo.getExchange());
                }
            }
            
            return Optional.of(info);
        }
        
        // Fallback to Yahoo Finance if Alpha Vantage completely fails
        return yahooFinanceProvider.getStockInfo(symbol);
    }

    @Override
    public List<StockPriceDTO> getHistoricalPrices(String symbol, LocalDate startDate, LocalDate endDate) {
        // Use Yahoo Finance for historical prices (fast, no rate limits)
        return yahooFinanceProvider.getHistoricalPrices(symbol, startDate, endDate);
    }

    @Override
    public Optional<BigDecimal> getCurrentPrice(String symbol) {
        // Use Yahoo Finance for current price
        return yahooFinanceProvider.getCurrentPrice(symbol);
    }

    @Override
    public Optional<StockFundamentalDTO> getFundamentals(String symbol) {
        // Use Alpha Vantage for comprehensive financial data
        // If Alpha Vantage fails, fallback to Yahoo Finance (limited data)
        Optional<StockFundamentalDTO> alphaVantageData = alphaVantageProvider.getFundamentals(symbol);
        
        if (alphaVantageData.isPresent()) {
            StockFundamentalDTO dto = alphaVantageData.get();
            
            // Try to get PE, EPS, shares from Yahoo Finance to fill in gaps
            Optional<StockFundamentalDTO> yahooData = yahooFinanceProvider.getFundamentals(symbol);
            if (yahooData.isPresent()) {
                StockFundamentalDTO yahoo = yahooData.get();
                
                // Fill in missing valuation metrics from Yahoo
                if (dto.getPeRatio() == null && yahoo.getPeRatio() != null) {
                    dto.setPeRatio(yahoo.getPeRatio());
                }
                if (dto.getPegRatio() == null && yahoo.getPegRatio() != null) {
                    dto.setPegRatio(yahoo.getPegRatio());
                }
                if (dto.getEpsTtm() == null && yahoo.getEpsTtm() != null) {
                    dto.setEpsTtm(yahoo.getEpsTtm());
                }
                if (dto.getSharesOutstanding() == null && yahoo.getSharesOutstanding() != null) {
                    dto.setSharesOutstanding(yahoo.getSharesOutstanding());
                }
            }
            
            return Optional.of(dto);
        }
        
        // Fallback to Yahoo Finance if Alpha Vantage is not available
        return yahooFinanceProvider.getFundamentals(symbol);
    }

    @Override
    public String getProviderName() {
        return "Hybrid (Yahoo Finance + Alpha Vantage)";
    }

    @Override
    public boolean isAvailable() {
        return yahooFinanceProvider.isAvailable() || alphaVantageProvider.isAvailable();
    }
}

