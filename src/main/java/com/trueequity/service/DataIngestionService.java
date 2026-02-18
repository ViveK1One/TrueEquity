package com.trueequity.service;

import com.trueequity.api.dto.StockFundamentalDTO;
import com.trueequity.api.dto.StockInfoDTO;
import com.trueequity.api.dto.StockPriceDTO;
import com.trueequity.api.provider.DataProvider;
import com.trueequity.repository.JdbcStockFinancialRepository;
import com.trueequity.repository.JdbcStockPriceRepository;
import com.trueequity.repository.JdbcStockRepository;
import com.trueequity.repository.JdbcStockScoreRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core data ingestion service
 * Handles fetching data from APIs and storing in database
 */
@Service
public class DataIngestionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static void log(String message) {
        System.out.println(LocalDateTime.now().format(FORMATTER) + " - " + message);
    }
    
    private static String formatMarketCap(Long marketCap) {
        if (marketCap == null) return "N/A";
        if (marketCap >= 1_000_000_000_000L) {
            return String.format("%.2fT", marketCap / 1_000_000_000_000.0);
        } else if (marketCap >= 1_000_000_000L) {
            return String.format("%.2fB", marketCap / 1_000_000_000.0);
        } else if (marketCap >= 1_000_000L) {
            return String.format("%.2fM", marketCap / 1_000_000.0);
        }
        return marketCap.toString();
    }

    private final DataProvider dataProvider;
    private final JdbcStockRepository stockRepository;
    private final JdbcStockPriceRepository stockPriceRepository;
    private final JdbcStockFinancialRepository stockFinancialRepository;
    private final JdbcStockScoreRepository stockScoreRepository;
    private final MetricsCalculationService metricsCalculationService;

    public DataIngestionService(
            DataProvider dataProvider,
            JdbcStockRepository stockRepository,
            JdbcStockPriceRepository stockPriceRepository,
            JdbcStockFinancialRepository stockFinancialRepository,
            JdbcStockScoreRepository stockScoreRepository,
            MetricsCalculationService metricsCalculationService) {
        this.dataProvider = dataProvider;
        this.stockRepository = stockRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.stockFinancialRepository = stockFinancialRepository;
        this.stockScoreRepository = stockScoreRepository;
        this.metricsCalculationService = metricsCalculationService;
    }

    /**
     * Ingest or update stock basic information.
     * When {@code force} is true (e.g. initial boot run), skips the "last updated" check and always fetches.
     */
    public void ingestStockInfo(String symbol) {
        ingestStockInfo(symbol, false);
    }

    /**
     * Ingest or update stock basic information
     * 
     * Logic:
     * - First time: INSERT new stock (if not exists)
     * - Next times: UPDATE existing stock (if exists)
     * - Uses ON CONFLICT DO UPDATE in database (upsert)
     * - Smart update: Only updates if last update was > 7 days ago (unless force=true)
     */
    public void ingestStockInfo(String symbol, boolean force) {
        try {
            if (!force) {
                LocalDateTime lastUpdated = stockRepository.getLastUpdated(symbol);
                if (lastUpdated != null) {
                    long daysSinceUpdate = java.time.Duration.between(lastUpdated, LocalDateTime.now()).toDays();
                    if (daysSinceUpdate < 7) {
                        log("Skipping stock info update for " + symbol + " - last updated " + daysSinceUpdate + " days ago (threshold: 7 days)");
                        return;
                    }
                }
            }

            log("Fetching stock info for: " + symbol);
            
            Optional<StockInfoDTO> infoOpt = dataProvider.getStockInfo(symbol);
            if (infoOpt.isEmpty()) {
                log("No stock info found for: " + symbol + " - creating minimal stock entry");
                // Create minimal stock entry to avoid foreign key constraint errors
                // This allows other data (prices, financials) to be inserted even if stock info is not available
                if (!stockRepository.exists(symbol)) {
                    stockRepository.upsertStock(
                        symbol,
                        symbol + " Corp", // Placeholder name
                        "NASDAQ", // Default exchange
                        null, // Sector unknown
                        null, // Industry unknown
                        null  // Market cap unknown
                    );
                    log("Created minimal stock entry for: " + symbol);
                }
                return;
            }

            StockInfoDTO info = infoOpt.get();
            
            // Validate - don't insert if name is null or empty
            if (info.getName() == null || info.getName().isEmpty() || info.getName().equals("null")) {
                log("Skipping " + symbol + " - name is null or empty");
                return;
            }
            
            // Validate exchange
            String exchange = info.getExchange();
            if (exchange == null || exchange.isEmpty()) {
                exchange = "NASDAQ";
            }
            
            // Check if stock already exists BEFORE upsert
            boolean exists = stockRepository.exists(symbol);

            // Upsert: INSERT if new, UPDATE if exists (handled by ON CONFLICT in SQL)
            stockRepository.upsertStock(
                symbol,
                info.getName(), // Already validated above
                exchange,
                info.getSector(), // Can be null
                info.getIndustry(), // Can be null
                info.getMarketCap() // Can be null
            );

            // Log what happened with all details
            if (exists) {
                log("UPDATED existing stock in 'stocks' table:");
            } else {
                log("INSERTED new stock in 'stocks' table:");
            }
            log("  Table: stocks");
            log("  Symbol: " + symbol);
            log("  Name: " + info.getName());
            log("  Exchange: " + exchange);
            log("  Sector: " + (info.getSector() != null ? info.getSector() : "NULL (not available)"));
            log("  Industry: " + (info.getIndustry() != null ? info.getIndustry() : "NULL (not available)"));
            log("  Market Cap: " + (info.getMarketCap() != null ? formatMarketCap(info.getMarketCap()) : "NULL (not available)"));
            
        } catch (Exception e) {
            log("Error ingesting stock info for " + symbol + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Ingest historical price data
     * 
     * Logic:
     * - First time: INSERT new price records
     * - Next times: UPDATE existing price records (same date)
     * - Uses ON CONFLICT DO UPDATE in database (upsert)
     */
    public void ingestHistoricalPrices(String symbol, LocalDate startDate, LocalDate endDate) {
        try {
            log("Fetching historical prices for " + symbol + " from " + startDate + " to " + endDate);
            
            List<StockPriceDTO> prices = dataProvider.getHistoricalPrices(symbol, startDate, endDate);
            
            if (prices.isEmpty()) {
                log("No price data returned for: " + symbol);
                return;
            }

            // Convert to batch insert format
            List<JdbcStockPriceRepository.PriceData> priceDataList = new ArrayList<>();
            for (StockPriceDTO priceDTO : prices) {
                priceDataList.add(new JdbcStockPriceRepository.PriceData(
                    priceDTO.getSymbol(),
                    priceDTO.getDate(),
                    priceDTO.getOpen(),
                    priceDTO.getHigh(),
                    priceDTO.getLow(),
                    priceDTO.getClose(),
                    priceDTO.getAdjustedClose(),
                    priceDTO.getVolume()
                ));
            }

            // Batch upsert: INSERT new prices, UPDATE existing prices (handled by ON CONFLICT in SQL)
            int originalCount = priceDataList.size();
            int[] results = stockPriceRepository.batchInsertPrices(priceDataList);
            int totalProcessed = results.length;
            int skipped = originalCount - totalProcessed;
            
            if (totalProcessed > 0) {
                log("Upserted " + totalProcessed + " price records in 'stock_prices' table for: " + symbol + " (INSERT new or UPDATE existing)");
            }
            if (skipped > 0) {
                log("Skipped " + skipped + " invalid price records for: " + symbol + " (null or invalid data)");
            }
            
        } catch (Exception e) {
            log("Error ingesting historical prices for " + symbol + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Ingest latest price data (for daily updates)
     */
    public void ingestLatestPrice(String symbol) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            
            // Fetch last 2 days to ensure we have today's data
            ingestHistoricalPrices(symbol, yesterday, today);
            
        } catch (Exception e) {
            log("Error ingesting latest price for " + symbol + ": " + e.getMessage());
        }
    }

    /**
     * Ingest fundamental data (includes EPS).
     * When {@code force} is true (e.g. initial boot run), skips the "last updated" check and always fetches.
     */
    public void ingestFundamentals(String symbol) {
        ingestFundamentals(symbol, false);
    }

    /**
     * Ingest fundamental data (includes EPS)
     * 
     * Logic:
     * - First time: INSERT new financial data
     * - Next times: UPDATE existing financial data (same period)
     * - Uses ON CONFLICT DO UPDATE in database (upsert)
     * - Smart update: Only updates if last update was > 12 hours ago (unless force=true)
     */
    public void ingestFundamentals(String symbol, boolean force) {
        try {
            // Ensure stock exists in stocks table first (to avoid foreign key constraint)
            if (!stockRepository.exists(symbol)) {
                log("Stock " + symbol + " not found in stocks table, creating minimal entry");
                stockRepository.upsertStock(
                    symbol,
                    symbol + " Corp",
                    "NASDAQ",
                    null,
                    null,
                    null
                );
            }

            if (!force) {
                LocalDateTime lastUpdated = stockFinancialRepository.getLastUpdated(symbol);
                if (lastUpdated != null) {
                    long hoursSinceUpdate = java.time.Duration.between(lastUpdated, LocalDateTime.now()).toHours();
                    if (hoursSinceUpdate < 12) {
                        log("Skipping fundamentals update for " + symbol + " - last updated " + hoursSinceUpdate + " hours ago (threshold: 12 hours)");
                        return;
                    }
                }
            }

            log("Fetching fundamentals for: " + symbol + " (using " + dataProvider.getProviderName() + ")");
            
            Optional<StockFundamentalDTO> fundamentalOpt = dataProvider.getFundamentals(symbol);
            if (fundamentalOpt.isEmpty()) {
                log("No fundamental data found for: " + symbol);
                return;
            }

            StockFundamentalDTO dto = fundamentalOpt.get();
            
            // Convert DTO to FinancialData record
            JdbcStockFinancialRepository.FinancialData financialData = 
                new JdbcStockFinancialRepository.FinancialData(
                    java.util.UUID.randomUUID(),
                    symbol,
                    dto.getPeriodType() != null ? dto.getPeriodType() : "annual",
                    dto.getPeriodEndDate() != null ? dto.getPeriodEndDate() : java.time.LocalDate.now(),
                    dto.getFiscalYear(),
                    dto.getFiscalQuarter(),
                    dto.getPeRatio(),
                    dto.getPegRatio(),
                    dto.getPriceToBook(),
                    dto.getPriceToSales(),
                    dto.getEvToEbitda(),
                    dto.getEpsTtm(),
                    dto.getEpsGrowthYoy(),
                    dto.getEpsGrowthQoq(),
                    dto.getRevenue(),
                    dto.getRevenueGrowthYoy(),
                    dto.getRevenueGrowthQoq(),
                    dto.getNetIncome(),
                    dto.getNetIncomeGrowthYoy(),
                    dto.getProfitMargin(),
                    dto.getTotalCash(),
                    dto.getTotalDebt(),
                    dto.getCashPerShare(),
                    dto.getDebtToEquity(),
                    dto.getCurrentRatio(),
                    dto.getRoe(),
                    dto.getRoic(),
                    dto.getRoa(),
                    dto.getGrossMargin(),
                    dto.getOperatingMargin(),
                    dto.getRevenueGrowth3y(),
                    dto.getEarningsGrowth3y(),
                    dto.getSharesOutstanding(),
                    dto.getFloatShares()
                );
            
            stockFinancialRepository.upsertFinancial(financialData);
            
            log("Upserted financial data in 'stock_financials' table for: " + symbol);
            log("  Table: stock_financials");
            log("  Symbol: " + symbol);
            log("  Period: " + financialData.periodType() + " - " + financialData.periodEndDate());
            log("  Revenue: " + (dto.getRevenue() != null ? formatMarketCap(dto.getRevenue()) : "NULL"));
            log("  Net Income: " + (dto.getNetIncome() != null ? formatMarketCap(dto.getNetIncome()) : "NULL"));
            log("  Total Cash: " + (dto.getTotalCash() != null ? formatMarketCap(dto.getTotalCash()) : "NULL"));
            log("  Total Debt: " + (dto.getTotalDebt() != null ? formatMarketCap(dto.getTotalDebt()) : "NULL"));
            log("  PE Ratio: " + (dto.getPeRatio() != null ? dto.getPeRatio() : "NULL"));
            log("  Debt/Equity: " + (dto.getDebtToEquity() != null ? dto.getDebtToEquity() : "NULL"));
            log("  ROE: " + (dto.getRoe() != null ? dto.getRoe() + "%" : "NULL"));
            log("  ROIC: " + (dto.getRoic() != null ? dto.getRoic() + "%" : "NULL"));
            log("  ROA: " + (dto.getRoa() != null ? dto.getRoa() + "%" : "NULL"));
            log("  Profit Margin: " + (dto.getProfitMargin() != null ? dto.getProfitMargin() + "%" : "NULL"));
            
        } catch (Exception e) {
            log("Error ingesting fundamentals for " + symbol + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Recalculate and store scores for a stock.
     * When {@code force} is true (e.g. initial boot run), skips the "last calculated" check and always recalculates.
     */
    public void calculateAndStoreScores(String symbol) {
        calculateAndStoreScores(symbol, false);
    }

    /**
     * Recalculate and store scores for a stock
     * 
     * Logic:
     * - Calculates scores from financial data and prices
     * - Stores in stock_scores table (replaces old score)
     * - Skips if calculated within last hour AND no new data (unless force=true)
     */
    public void calculateAndStoreScores(String symbol, boolean force) {
        try {
            if (!force) {
                LocalDateTime lastCalculated = stockScoreRepository.getLastCalculated(symbol);
                LocalDateTime lastFinancialUpdate = stockFinancialRepository.getLastUpdated(symbol);
                LocalDateTime lastPriceUpdate = stockPriceRepository.getLastPriceUpdate(symbol);

                if (lastCalculated != null) {
                    long hoursSinceCalculation = java.time.Duration.between(lastCalculated, LocalDateTime.now()).toHours();
                    boolean hasNewFinancialData = lastFinancialUpdate != null && lastCalculated.isBefore(lastFinancialUpdate);
                    boolean hasNewPriceData = lastPriceUpdate != null && lastCalculated.isBefore(lastPriceUpdate);

                    if (hoursSinceCalculation < 1 && !hasNewFinancialData && !hasNewPriceData) {
                        log("Skipping score calculation for " + symbol + " - last calculated " + hoursSinceCalculation + " hours ago and no new data");
                        return;
                    }
                }
            }

            log("Calculating scores for: " + symbol);
            
            Optional<JdbcStockScoreRepository.ScoreData> scoreOpt = 
                metricsCalculationService.calculateScores(symbol);
            
            if (scoreOpt.isEmpty()) {
                // Try to get more details about what's missing
                Optional<JdbcStockFinancialRepository.FinancialData> financialCheck = 
                    stockFinancialRepository.findLatestBySymbol(symbol);
                BigDecimal priceCheck = stockPriceRepository.getLatestPrice(symbol);
                
                String missing = "";
                if (financialCheck.isEmpty()) missing += "financial data ";
                if (priceCheck == null) missing += "price data ";
                
                log("Cannot calculate scores for " + symbol + " - missing: " + (missing.isEmpty() ? "unknown" : missing));
                return;
            }
            
            JdbcStockScoreRepository.ScoreData score = scoreOpt.get();
            stockScoreRepository.upsertScore(score);
            
            log("Upserted scores in 'stock_scores' table for: " + symbol);
            log("  Table: stock_scores");
            log("  Symbol: " + symbol);
            log("  Overall Score: " + score.overallScore() + " (" + score.overallGrade() + ")");
            log("  Valuation: " + score.valuationCategory() + " (" + score.valuationScore() + ")");
            log("  Health: " + score.healthGrade() + " (" + score.healthScore() + ")");
            log("  Growth: " + score.growthGrade() + " (" + score.growthScore() + ")");
            log("  Risk: " + score.riskGrade() + " (" + score.riskScore() + ")");
            
        } catch (Exception e) {
            log("Error calculating scores for " + symbol + ": " + e.getMessage());
        }
    }
}

