package com.trueequity.service;

import com.trueequity.repository.JdbcStockFinancialRepository;
import com.trueequity.repository.JdbcStockPriceRepository;
import com.trueequity.repository.JdbcStockScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MetricsCalculationService
 * Tests score calculation logic for valuation, health, growth, and risk
 */
@ExtendWith(MockitoExtension.class)
class MetricsCalculationServiceTest {

    @Mock
    private JdbcStockPriceRepository stockPriceRepository;

    @Mock
    private JdbcStockFinancialRepository stockFinancialRepository;

    private MetricsCalculationService service;

    @BeforeEach
    void setUp() {
        service = new MetricsCalculationService(stockPriceRepository, stockFinancialRepository);
    }

    @Test
    void testCalculateScores_WithValidData_ReturnsScoreData() {
        // Arrange
        String symbol = "AAPL";
        BigDecimal latestPrice = BigDecimal.valueOf(150.00);
        
        // Create FinancialData with all required fields (using UUID and nulls for optional fields)
        JdbcStockFinancialRepository.FinancialData financialData = 
            new JdbcStockFinancialRepository.FinancialData(
                java.util.UUID.randomUUID(), // id
                symbol,                      // symbol
                "QUARTERLY",                 // periodType
                LocalDate.now(),             // periodEndDate
                2024,                        // fiscalYear
                1,                           // fiscalQuarter
                BigDecimal.valueOf(30.0),    // peRatio
                BigDecimal.valueOf(1.5),     // pegRatio
                BigDecimal.valueOf(2.0),     // priceToBook
                null,                        // priceToSales
                null,                        // evToEbitda
                null,                        // epsTtm
                BigDecimal.valueOf(15.0),    // epsGrowthYoy
                null,                        // epsGrowthQoq
                null,                        // revenue
                BigDecimal.valueOf(10.0),    // revenueGrowthYoy
                null,                        // revenueGrowthQoq
                null,                        // netIncome
                null,                        // netIncomeGrowthYoy
                null,                        // profitMargin
                null,                        // totalCash
                null,                        // totalDebt
                null,                        // cashPerShare
                BigDecimal.valueOf(0.5),     // debtToEquity
                BigDecimal.valueOf(2.0),     // currentRatio
                BigDecimal.valueOf(25.0),    // roe
                BigDecimal.valueOf(20.0),    // roic
                null,                        // roa
                null,                        // grossMargin
                null,                        // operatingMargin
                null,                        // revenueGrowth3y
                null,                        // earningsGrowth3y
                null,                        // sharesOutstanding
                null                         // floatShares
            );

        when(stockFinancialRepository.findLatestBySymbol(symbol))
            .thenReturn(Optional.of(financialData));
        when(stockPriceRepository.getLatestPrice(symbol))
            .thenReturn(latestPrice);

        // Act
        Optional<JdbcStockScoreRepository.ScoreData> result = service.calculateScores(symbol);

        // Assert
        assertTrue(result.isPresent());
        JdbcStockScoreRepository.ScoreData scoreData = result.get();
        assertEquals(symbol, scoreData.symbol());
        assertNotNull(scoreData.overallScore());
        assertTrue(scoreData.overallScore().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(scoreData.overallScore().compareTo(BigDecimal.valueOf(100)) <= 0);
        assertNotNull(scoreData.overallGrade());
        assertNotNull(scoreData.valuationScore());
        assertNotNull(scoreData.healthScore());
        assertNotNull(scoreData.growthScore());
        assertNotNull(scoreData.riskScore());
    }

    @Test
    void testCalculateScores_WithNoFinancialData_ReturnsEmpty() {
        // Arrange
        String symbol = "INVALID";
        when(stockFinancialRepository.findLatestBySymbol(symbol))
            .thenReturn(Optional.empty());

        // Act
        Optional<JdbcStockScoreRepository.ScoreData> result = service.calculateScores(symbol);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testCalculateScores_WithNoPriceData_ReturnsEmpty() {
        // Arrange
        String symbol = "AAPL";
        JdbcStockFinancialRepository.FinancialData financialData = 
            new JdbcStockFinancialRepository.FinancialData(
                java.util.UUID.randomUUID(), // id
                symbol,                      // symbol
                "QUARTERLY",                 // periodType
                LocalDate.now(),             // periodEndDate
                2024,                        // fiscalYear
                1,                           // fiscalQuarter
                BigDecimal.valueOf(30.0),    // peRatio
                BigDecimal.valueOf(1.5),    // pegRatio
                BigDecimal.valueOf(2.0),     // priceToBook
                null,                        // priceToSales
                null,                        // evToEbitda
                null,                        // epsTtm
                BigDecimal.valueOf(15.0),    // epsGrowthYoy
                null,                        // epsGrowthQoq
                null,                        // revenue
                BigDecimal.valueOf(10.0),    // revenueGrowthYoy
                null,                        // revenueGrowthQoq
                null,                        // netIncome
                null,                        // netIncomeGrowthYoy
                null,                        // profitMargin
                null,                        // totalCash
                null,                        // totalDebt
                null,                        // cashPerShare
                BigDecimal.valueOf(0.5),     // debtToEquity
                BigDecimal.valueOf(2.0),    // currentRatio
                BigDecimal.valueOf(25.0),   // roe
                BigDecimal.valueOf(20.0),   // roic
                null,                        // roa
                null,                        // grossMargin
                null,                        // operatingMargin
                null,                        // revenueGrowth3y
                null,                        // earningsGrowth3y
                null,                        // sharesOutstanding
                null                         // floatShares
            );

        when(stockFinancialRepository.findLatestBySymbol(symbol))
            .thenReturn(Optional.of(financialData));
        when(stockPriceRepository.getLatestPrice(symbol))
            .thenReturn(null);

        // Act
        Optional<JdbcStockScoreRepository.ScoreData> result = service.calculateScores(symbol);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testCalculateScores_WithNullFinancialFields_HandlesGracefully() {
        // Arrange
        String symbol = "AAPL";
        BigDecimal latestPrice = BigDecimal.valueOf(150.00);
        
        // Financial data with null fields (simulating missing data)
        JdbcStockFinancialRepository.FinancialData financialData = 
            new JdbcStockFinancialRepository.FinancialData(
                java.util.UUID.randomUUID(), // id
                symbol,                      // symbol
                "QUARTERLY",                 // periodType
                LocalDate.now(),             // periodEndDate
                2024,                        // fiscalYear
                1,                           // fiscalQuarter
                null,                        // peRatio null
                null,                        // pegRatio null
                null,                        // priceToBook null
                null,                        // priceToSales
                null,                        // evToEbitda
                null,                        // epsTtm
                null,                        // epsGrowthYoy null
                null,                        // epsGrowthQoq
                null,                        // revenue
                null,                        // revenueGrowthYoy null
                null,                        // revenueGrowthQoq
                null,                        // netIncome
                null,                        // netIncomeGrowthYoy
                null,                        // profitMargin
                null,                        // totalCash
                null,                        // totalDebt
                null,                        // cashPerShare
                null,                        // debtToEquity null
                null,                        // currentRatio null
                null,                        // roe null
                null,                        // roic null
                null,                        // roa
                null,                        // grossMargin
                null,                        // operatingMargin
                null,                        // revenueGrowth3y
                null,                        // earningsGrowth3y
                null,                        // sharesOutstanding
                null                         // floatShares
            );

        when(stockFinancialRepository.findLatestBySymbol(symbol))
            .thenReturn(Optional.of(financialData));
        when(stockPriceRepository.getLatestPrice(symbol))
            .thenReturn(latestPrice);

        // Act
        Optional<JdbcStockScoreRepository.ScoreData> result = service.calculateScores(symbol);

        // Assert
        assertTrue(result.isPresent());
        // Should still calculate scores with neutral values for missing data
        JdbcStockScoreRepository.ScoreData scoreData = result.get();
        assertNotNull(scoreData.overallScore());
    }

    @Test
    void testCalculateScores_WithHighQualityStock_ReturnsHighScores() {
        // Arrange
        String symbol = "QUALITY";
        BigDecimal latestPrice = BigDecimal.valueOf(200.00);
        
        // High-quality stock: low PE, low PEG, low debt, high growth
        JdbcStockFinancialRepository.FinancialData financialData = 
            new JdbcStockFinancialRepository.FinancialData(
                java.util.UUID.randomUUID(), // id
                symbol,                      // symbol
                "QUARTERLY",                 // periodType
                LocalDate.now(),             // periodEndDate
                2024,                        // fiscalYear
                1,                           // fiscalQuarter
                BigDecimal.valueOf(12.0),   // Low PE (good)
                BigDecimal.valueOf(0.8),    // Low PEG (undervalued)
                BigDecimal.valueOf(1.5),    // Reasonable P/B
                null,                        // priceToSales
                null,                        // evToEbitda
                null,                        // epsTtm
                BigDecimal.valueOf(30.0),    // High EPS growth
                null,                        // epsGrowthQoq
                null,                        // revenue
                BigDecimal.valueOf(25.0),   // High revenue growth
                null,                        // revenueGrowthQoq
                null,                        // netIncome
                null,                        // netIncomeGrowthYoy
                null,                        // profitMargin
                null,                        // totalCash
                null,                        // totalDebt
                null,                        // cashPerShare
                BigDecimal.valueOf(0.3),     // Low debt (excellent)
                BigDecimal.valueOf(2.5),    // High current ratio (excellent)
                BigDecimal.valueOf(30.0),    // High ROE
                BigDecimal.valueOf(25.0),    // High ROIC
                null,                        // roa
                null,                        // grossMargin
                null,                        // operatingMargin
                null,                        // revenueGrowth3y
                null,                        // earningsGrowth3y
                null,                        // sharesOutstanding
                null                         // floatShares
            );

        when(stockFinancialRepository.findLatestBySymbol(symbol))
            .thenReturn(Optional.of(financialData));
        when(stockPriceRepository.getLatestPrice(symbol))
            .thenReturn(latestPrice);

        // Act
        Optional<JdbcStockScoreRepository.ScoreData> result = service.calculateScores(symbol);

        // Assert
        assertTrue(result.isPresent());
        JdbcStockScoreRepository.ScoreData scoreData = result.get();
        // High-quality stock should have high overall score
        assertTrue(scoreData.overallScore().compareTo(BigDecimal.valueOf(70)) > 0);
        // Should get good grades
        assertTrue(scoreData.overallGrade().equals("A") || 
                   scoreData.overallGrade().equals("B"));
    }

    @Test
    void testCalculateScores_WithLowQualityStock_ReturnsLowScores() {
        // Arrange
        String symbol = "POOR";
        BigDecimal latestPrice = BigDecimal.valueOf(50.00);
        
        // Low-quality stock: high PE, high PEG, high debt, low growth
        JdbcStockFinancialRepository.FinancialData financialData = 
            new JdbcStockFinancialRepository.FinancialData(
                java.util.UUID.randomUUID(), // id
                symbol,                      // symbol
                "QUARTERLY",                 // periodType
                LocalDate.now(),             // periodEndDate
                2024,                        // fiscalYear
                1,                           // fiscalQuarter
                BigDecimal.valueOf(50.0),   // High PE (expensive)
                BigDecimal.valueOf(4.0),    // High PEG (expensive)
                BigDecimal.valueOf(5.0),    // High P/B
                null,                        // priceToSales
                null,                        // evToEbitda
                null,                        // epsTtm
                BigDecimal.valueOf(1.0),    // Low EPS growth
                null,                        // epsGrowthQoq
                null,                        // revenue
                BigDecimal.valueOf(2.0),    // Low revenue growth
                null,                        // revenueGrowthQoq
                null,                        // netIncome
                null,                        // netIncomeGrowthYoy
                null,                        // profitMargin
                null,                        // totalCash
                null,                        // totalDebt
                null,                        // cashPerShare
                BigDecimal.valueOf(3.0),     // High debt (risky)
                BigDecimal.valueOf(0.5),    // Low current ratio (risky)
                BigDecimal.valueOf(5.0),    // Low ROE
                BigDecimal.valueOf(3.0),    // Low ROIC
                null,                        // roa
                null,                        // grossMargin
                null,                        // operatingMargin
                null,                        // revenueGrowth3y
                null,                        // earningsGrowth3y
                null,                        // sharesOutstanding
                null                         // floatShares
            );

        when(stockFinancialRepository.findLatestBySymbol(symbol))
            .thenReturn(Optional.of(financialData));
        when(stockPriceRepository.getLatestPrice(symbol))
            .thenReturn(latestPrice);

        // Act
        Optional<JdbcStockScoreRepository.ScoreData> result = service.calculateScores(symbol);

        // Assert
        assertTrue(result.isPresent());
        JdbcStockScoreRepository.ScoreData scoreData = result.get();
        // Low-quality stock should have low overall score
        assertTrue(scoreData.overallScore().compareTo(BigDecimal.valueOf(50)) < 0);
        // Should get lower grades
        assertTrue(scoreData.overallGrade().equals("D") || 
                   scoreData.overallGrade().equals("F"));
    }
}
