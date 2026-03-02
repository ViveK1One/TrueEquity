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
 * - Financial Modeling Prep (FMP) for comprehensive financial data (250 requests/day)
 */
@Component
public class HybridDataProvider implements DataProvider {

    private final YahooFinanceProvider yahooFinanceProvider;
    private final FinancialModelingPrepProvider fmpProvider;

    public HybridDataProvider(YahooFinanceProvider yahooFinanceProvider, 
                             FinancialModelingPrepProvider fmpProvider) {
        this.yahooFinanceProvider = yahooFinanceProvider;
        this.fmpProvider = fmpProvider;
    }

    @Override
    public Optional<StockInfoDTO> getStockInfo(String symbol) {
        // Use FMP as primary source for sector, industry, market cap
        // Use Yahoo Finance only for name and exchange if FMP doesn't have them
        Optional<StockInfoDTO> fmpInfo = fmpProvider.getStockInfo(symbol);
        
        if (fmpInfo.isPresent()) {
            StockInfoDTO info = fmpInfo.get();
            
            // If FMP has name and exchange, use it directly
            if (info.getName() != null && !info.getName().isEmpty() && 
                info.getExchange() != null && !info.getExchange().isEmpty()) {
                return Optional.of(info);
            }
            
            // If FMP doesn't have name/exchange, try Yahoo Finance
            Optional<StockInfoDTO> yahooInfo = yahooFinanceProvider.getStockInfo(symbol);
            if (yahooInfo.isPresent()) {
                StockInfoDTO yahoo = yahooInfo.get();
                // Fill in name and exchange from Yahoo, keep sector/industry/market cap from FMP
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
        
        // Fallback to Yahoo Finance if FMP completely fails
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
        // Use FMP for comprehensive financial data
        // If FMP fails, fallback to Yahoo Finance (limited data)
        Optional<StockFundamentalDTO> fmpData = fmpProvider.getFundamentals(symbol);
        
        if (fmpData.isPresent()) {
            StockFundamentalDTO dto = fmpData.get();
            
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
        
        // Fallback to Yahoo Finance if FMP is not available
        return yahooFinanceProvider.getFundamentals(symbol);
    }

    @Override
    public String getProviderName() {
        return "Hybrid (Yahoo Finance + FMP)";
    }

    @Override
    public boolean isAvailable() {
        return yahooFinanceProvider.isAvailable() || fmpProvider.isAvailable();
    }
}

