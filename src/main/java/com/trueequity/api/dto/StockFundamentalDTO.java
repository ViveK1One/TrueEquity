package com.trueequity.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for fundamental financial data
 * Matches stock_financials table schema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockFundamentalDTO {
    private String symbol;
    private String periodType; // 'quarterly' or 'annual'
    private LocalDate periodEndDate;
    private Integer fiscalYear;
    private Integer fiscalQuarter;
    
    // Valuation Metrics
    private BigDecimal peRatio;
    private BigDecimal pegRatio;
    private BigDecimal priceToBook;
    private BigDecimal priceToSales;
    private BigDecimal evToEbitda;
    
    // Earnings
    private BigDecimal epsTtm;
    private BigDecimal epsGrowthYoy;
    private BigDecimal epsGrowthQoq;
    
    // Revenue & Profit
    private Long revenue;
    private BigDecimal revenueGrowthYoy;
    private BigDecimal revenueGrowthQoq;
    private Long netIncome;
    private BigDecimal netIncomeGrowthYoy;
    private BigDecimal profitMargin;
    
    // Balance Sheet
    private Long totalCash;
    private Long totalDebt;
    private BigDecimal cashPerShare;
    private BigDecimal debtToEquity;
    private BigDecimal currentRatio;
    
    // Profitability
    private BigDecimal roe;
    private BigDecimal roic;
    private BigDecimal roa;
    private BigDecimal grossMargin;
    private BigDecimal operatingMargin;
    
    // Growth Metrics
    private BigDecimal revenueGrowth3y;
    private BigDecimal earningsGrowth3y;
    
    // Market Data
    private Long sharesOutstanding;
    private Long floatShares;
}

