package com.trueequity.service;

import com.trueequity.repository.JdbcStockFinancialRepository;
import com.trueequity.repository.JdbcStockPriceRepository;
import com.trueequity.repository.JdbcStockScoreRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for calculating derived metrics and scores for stock analysis.
 * 
 * <p>This service pre-computes various financial scores to enable fast frontend queries.
 * It calculates four main categories of scores:
 * <ul>
 *   <li><b>Valuation Score</b>: Based on P/E ratio, PEG ratio, and Price-to-Book ratio</li>
 *   <li><b>Financial Health Score</b>: Based on Debt-to-Equity and Current Ratio</li>
 *   <li><b>Growth Score</b>: Based on Revenue Growth and EPS Growth (YoY)</li>
 *   <li><b>Risk Score</b>: Based on debt levels and liquidity ratios</li>
 * </ul>
 * 
 * <p>The overall score is a weighted average of these four categories:
 * <ul>
 *   <li>Valuation: 25% weight</li>
 *   <li>Financial Health: 30% weight</li>
 *   <li>Growth: 30% weight</li>
 *   <li>Risk (inverted): 15% weight</li>
 * </ul>
 * 
 * <p>Each score is normalized to a 0-100 scale and assigned a letter grade (A, B, C, D, F).
 * 
 * @author TrueEquity Development Team
 * @version 1.0
 * @since 1.0
 */
@Service
public class MetricsCalculationService {

    private final JdbcStockPriceRepository stockPriceRepository;
    private final JdbcStockFinancialRepository stockFinancialRepository;

    public MetricsCalculationService(
            JdbcStockPriceRepository stockPriceRepository,
            JdbcStockFinancialRepository stockFinancialRepository) {
        this.stockPriceRepository = stockPriceRepository;
        this.stockFinancialRepository = stockFinancialRepository;
    }

    /**
     * Calculates all financial scores for a given stock symbol.
     * 
     * <p>This method:
     * <ol>
     *   <li>Retrieves the latest financial data from the database</li>
     *   <li>Retrieves the latest stock price</li>
     *   <li>Calculates valuation, health, growth, and risk scores</li>
     *   <li>Computes the overall weighted score</li>
     *   <li>Assigns letter grades to each category</li>
     * </ol>
     * 
     * @param symbol The stock symbol (e.g., "AAPL", "MSFT")
     * @return Optional containing ScoreData if calculation succeeds, empty if financial data or price is missing
     * @throws Exception If database access fails or calculation error occurs
     */
    public Optional<JdbcStockScoreRepository.ScoreData> calculateScores(String symbol) {
        try {
            // Get latest financial data from database
            // Score calculation is independent - it just reads existing data
            Optional<JdbcStockFinancialRepository.FinancialData> financialOpt = 
                stockFinancialRepository.findLatestBySymbol(symbol);
            
            if (financialOpt.isEmpty()) {
                return Optional.empty();
            }
            
            JdbcStockFinancialRepository.FinancialData financial = financialOpt.get();
            
            // Get latest price for volatility calculation
            BigDecimal latestPrice = stockPriceRepository.getLatestPrice(symbol);
            if (latestPrice == null) {
                return Optional.empty();
            }
            
            // Calculate Valuation Score (combines PE and PEG ratios)
            BigDecimal peRatio = financial.peRatio();
            BigDecimal pegRatio = financial.pegRatio();
            BigDecimal priceToBook = financial.priceToBook();
            String valuationCategory = determineValuationCategory(peRatio, pegRatio);
            BigDecimal valuationScore = calculateValuationScore(peRatio, pegRatio, priceToBook);
            BigDecimal peScore = calculatePeScore(peRatio);
            BigDecimal pegScore = calculatePegScore(pegRatio);
            
            // Calculate Financial Health Score
            // NOTE: debtToEquity and currentRatio are NULL from chart endpoint, so scores will be limited
            BigDecimal debtToEquity = financial.debtToEquity();
            BigDecimal currentRatio = financial.currentRatio();
            BigDecimal healthScore = calculateFinancialHealthScore(debtToEquity, currentRatio);
            String healthGrade = calculateGrade(healthScore);
            BigDecimal debtScore = calculateDebtScore(debtToEquity);
            
            // Calculate Growth Score
            // NOTE: revenueGrowthYoy and epsGrowthYoy are NULL from chart endpoint
            BigDecimal revenueGrowthYoy = financial.revenueGrowthYoy();
            BigDecimal epsGrowthYoy = financial.epsGrowthYoy();
            BigDecimal growthScore = calculateGrowthScore(revenueGrowthYoy, epsGrowthYoy);
            String growthGrade = calculateGrade(growthScore);
            BigDecimal growthRateScore = calculateGrowthRateScore(revenueGrowthYoy, epsGrowthYoy);
            
            // Calculate Risk Score
            // NOTE: debtToEquity and currentRatio are NULL, so risk score will be minimal
            BigDecimal riskScore = calculateRiskScore(debtToEquity, financial.currentRatio());
            String riskGrade = calculateGrade(riskScore);
            BigDecimal volatilityScore = BigDecimal.ZERO; // TODO: Calculate from price history
            
            // Calculate Profitability Score
            // NOTE: roe and roic are NULL from chart endpoint
            BigDecimal roe = financial.roe();
            BigDecimal roic = financial.roic();
            BigDecimal profitabilityScore = calculateProfitabilityScore(roe, roic);
            
            // Calculate Overall Score
            BigDecimal overallScore = calculateOverallScore(valuationScore, healthScore, growthScore, riskScore);
            String overallGrade = calculateGrade(overallScore);
            
            JdbcStockScoreRepository.ScoreData scoreData = new JdbcStockScoreRepository.ScoreData(
                UUID.randomUUID(),
                symbol,
                LocalDateTime.now(),
                valuationCategory,
                valuationScore,
                healthScore,
                healthGrade,
                growthScore,
                growthGrade,
                riskScore,
                riskGrade,
                overallScore,
                overallGrade,
                peScore,
                pegScore,
                debtScore,
                profitabilityScore,
                growthRateScore,
                volatilityScore
            );
            
            return Optional.of(scoreData);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Determines the valuation category (cheap, fair, expensive) based on PE and PEG ratios.
     * 
     * <p>Valuation logic:
     * <ul>
     *   <li><b>PEG Ratio</b> (preferred): PEG < 1 = "cheap", 1-2 = "fair", > 2 = "expensive"</li>
     *   <li><b>PE Ratio</b> (fallback if PEG unavailable): PE < 15 = "cheap", 15-25 = "fair", > 25 = "expensive"</li>
     * </ul>
     * 
     * <p>PEG ratio is preferred because it accounts for growth rate, making it more accurate than PE alone.
     * 
     * @param peRatio Price-to-Earnings ratio (can be null)
     * @param pegRatio Price/Earnings to Growth ratio (can be null)
     * @return "cheap", "fair", "expensive", or "N/A" if both ratios are null
     */
    private String determineValuationCategory(BigDecimal peRatio, BigDecimal pegRatio) {
        // PEG ratio is more accurate as it accounts for growth
        if (pegRatio != null && pegRatio.compareTo(BigDecimal.ZERO) > 0) {
            if (pegRatio.compareTo(BigDecimal.valueOf(1.0)) < 0) return "cheap";
            if (pegRatio.compareTo(BigDecimal.valueOf(2.0)) < 0) return "fair";
            return "expensive";
        }
        // Fallback to PE ratio if PEG is not available
        if (peRatio == null) return "N/A";
        if (peRatio.compareTo(BigDecimal.valueOf(15)) < 0) return "cheap";
        if (peRatio.compareTo(BigDecimal.valueOf(25)) < 0) return "fair";
        return "expensive";
    }
    
    /**
     * Calculates the valuation score (0-100) by combining PE, PEG, and Price-to-Book ratios.
     * 
     * <p>Scoring weights:
     * <ul>
     *   <li>PE Ratio: 40% weight</li>
     *   <li>PEG Ratio: 40% weight (most important for valuation)</li>
     *   <li>Price-to-Book: 20% weight</li>
     * </ul>
     * 
     * <p>Lower ratios indicate better valuation. PEG < 1 is considered ideal (undervalued).
     * 
     * @param peRatio Price-to-Earnings ratio (can be null)
     * @param pegRatio Price/Earnings to Growth ratio (can be null)
     * @param priceToBook Price-to-Book ratio (can be null)
     * @return Valuation score between 0-100, or 50 (neutral) if all ratios are null
     */
    private BigDecimal calculateValuationScore(BigDecimal peRatio, BigDecimal pegRatio, BigDecimal priceToBook) {
        BigDecimal score = BigDecimal.ZERO;
        int factors = 0;
        
        // PE Ratio scoring (40% weight)
        if (peRatio != null && peRatio.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal peScore = calculatePeScore(peRatio);
            score = score.add(peScore.multiply(BigDecimal.valueOf(0.4)));
            factors++;
        }
        
        // PEG Ratio scoring (40% weight) - Best practice: PEG < 1 is undervalued
        if (pegRatio != null && pegRatio.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pegScore = calculatePegScore(pegRatio);
            score = score.add(pegScore.multiply(BigDecimal.valueOf(0.4)));
            factors++;
        }
        
        // Price-to-Book scoring (20% weight)
        if (priceToBook != null && priceToBook.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pbScore = calculatePriceToBookScore(priceToBook);
            score = score.add(pbScore.multiply(BigDecimal.valueOf(0.2)));
            factors++;
        }
        
        // If no factors available, return neutral score
        if (factors == 0) {
            return BigDecimal.valueOf(50);
        }
        
        // Normalize to 0-100 range
        return score.min(BigDecimal.valueOf(100));
    }
    
    /**
     * Calculates PE ratio score (0-100).
     * 
     * <p>Scoring scale:
     * <ul>
     *   <li>PE < 10: 100 points (excellent)</li>
     *   <li>PE 10-15: 90 points (very good)</li>
     *   <li>PE 15-20: 75 points (good)</li>
     *   <li>PE 20-30: 55 points (fair)</li>
     *   <li>PE 30-40: 35 points (poor)</li>
     *   <li>PE > 40: 20 points (very poor)</li>
     * </ul>
     * 
     * @param peRatio Price-to-Earnings ratio (can be null)
     * @return PE score between 0-100, or 50 (neutral) if PE ratio is null or invalid
     */
    private BigDecimal calculatePeScore(BigDecimal peRatio) {
        if (peRatio == null || peRatio.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.valueOf(50);
        // Lower PE is better, score 0-100
        if (peRatio.compareTo(BigDecimal.valueOf(10)) < 0) return BigDecimal.valueOf(100);
        if (peRatio.compareTo(BigDecimal.valueOf(15)) < 0) return BigDecimal.valueOf(90);
        if (peRatio.compareTo(BigDecimal.valueOf(20)) < 0) return BigDecimal.valueOf(75);
        if (peRatio.compareTo(BigDecimal.valueOf(30)) < 0) return BigDecimal.valueOf(55);
        if (peRatio.compareTo(BigDecimal.valueOf(40)) < 0) return BigDecimal.valueOf(35);
        return BigDecimal.valueOf(20);
    }
    
    /**
     * Calculates PEG ratio score (0-100).
     * 
     * <p>PEG (Price/Earnings to Growth) is a more accurate valuation metric than PE alone
     * because it accounts for the company's growth rate.
     * 
     * <p>Scoring scale (based on financial research):
     * <ul>
     *   <li>PEG < 1: 100 points (undervalued - ideal buying opportunity)</li>
     *   <li>PEG 1-2: 80 points (fair value)</li>
     *   <li>PEG 2-3: 60 points (slightly expensive)</li>
     *   <li>PEG > 3: 40 points (expensive - overvalued)</li>
     * </ul>
     * 
     * @param pegRatio Price/Earnings to Growth ratio (can be null)
     * @return PEG score between 0-100, or 50 (neutral) if PEG ratio is null or invalid
     */
    private BigDecimal calculatePegScore(BigDecimal pegRatio) {
        if (pegRatio == null || pegRatio.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.valueOf(50);
        // PEG < 1 is ideal (undervalued relative to growth)
        if (pegRatio.compareTo(BigDecimal.valueOf(1.0)) < 0) return BigDecimal.valueOf(100);
        if (pegRatio.compareTo(BigDecimal.valueOf(2.0)) < 0) return BigDecimal.valueOf(80);
        if (pegRatio.compareTo(BigDecimal.valueOf(3.0)) < 0) return BigDecimal.valueOf(60);
        return BigDecimal.valueOf(40);
    }
    
    /**
     * Calculates Price-to-Book ratio score (0-100).
     * 
     * <p>Price-to-Book compares a stock's market value to its book value (assets minus liabilities).
     * Lower ratios generally indicate better value.
     * 
     * <p>Scoring scale:
     * <ul>
     *   <li>P/B < 1: 100 points (undervalued - trading below book value)</li>
     *   <li>P/B 1-2: 80 points (fair value)</li>
     *   <li>P/B 2-3: 60 points (slightly expensive)</li>
     *   <li>P/B > 3: 40 points (expensive)</li>
     * </ul>
     * 
     * @param priceToBook Price-to-Book ratio (can be null)
     * @return P/B score between 0-100, or 50 (neutral) if P/B ratio is null or invalid
     */
    private BigDecimal calculatePriceToBookScore(BigDecimal priceToBook) {
        if (priceToBook == null || priceToBook.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.valueOf(50);
        if (priceToBook.compareTo(BigDecimal.valueOf(1.0)) < 0) return BigDecimal.valueOf(100);
        if (priceToBook.compareTo(BigDecimal.valueOf(2.0)) < 0) return BigDecimal.valueOf(80);
        if (priceToBook.compareTo(BigDecimal.valueOf(3.0)) < 0) return BigDecimal.valueOf(60);
        return BigDecimal.valueOf(40);
    }
    
    /**
     * Calculates Financial Health Score (0-100) by combining Debt-to-Equity and Current Ratio.
     * 
     * <p>This score measures a company's financial stability and ability to meet short-term obligations.
     * 
     * <p>Debt-to-Equity scoring:
     * <ul>
     *   <li>D/E < 0.5: 50 points (excellent - low debt)</li>
     *   <li>D/E 0.5-1.0: 30 points (good)</li>
     *   <li>D/E 1.0-2.0: 15 points (moderate)</li>
     *   <li>D/E > 2.0: 5 points (high debt - risky)</li>
     * </ul>
     * 
     * <p>Current Ratio scoring:
     * <ul>
     *   <li>CR >= 2.0: 50 points (excellent liquidity)</li>
     *   <li>CR 1.5-2.0: 30 points (good liquidity)</li>
     *   <li>CR 1.0-1.5: 15 points (adequate liquidity)</li>
     *   <li>CR < 1.0: 5 points (poor liquidity - may struggle to pay bills)</li>
     * </ul>
     * 
     * @param debtToEquity Debt-to-Equity ratio (can be null)
     * @param currentRatio Current Ratio (can be null)
     * @return Financial health score between 0-100, or 50 (neutral) if both ratios are null
     */
    private BigDecimal calculateFinancialHealthScore(BigDecimal debtToEquity, BigDecimal currentRatio) {
        BigDecimal score = BigDecimal.ZERO;
        int factors = 0;
        
        // Debt to Equity: lower is better (best practice: < 0.5 is excellent)
        if (debtToEquity != null) {
            if (debtToEquity.compareTo(BigDecimal.valueOf(0.5)) < 0) {
                score = score.add(BigDecimal.valueOf(50));
            } else if (debtToEquity.compareTo(BigDecimal.valueOf(1.0)) < 0) {
                score = score.add(BigDecimal.valueOf(30));
            } else if (debtToEquity.compareTo(BigDecimal.valueOf(2.0)) < 0) {
                score = score.add(BigDecimal.valueOf(15));
            } else {
                score = score.add(BigDecimal.valueOf(5));
            }
            factors++;
        }
        
        // Current Ratio: higher is better (best practice: 1.5-3.0 is ideal, > 2 is excellent)
        if (currentRatio != null) {
            if (currentRatio.compareTo(BigDecimal.valueOf(2.0)) >= 0) {
                score = score.add(BigDecimal.valueOf(50));
            } else if (currentRatio.compareTo(BigDecimal.valueOf(1.5)) >= 0) {
                score = score.add(BigDecimal.valueOf(30));
            } else if (currentRatio.compareTo(BigDecimal.valueOf(1.0)) >= 0) {
                score = score.add(BigDecimal.valueOf(15));
            } else {
                score = score.add(BigDecimal.valueOf(5));
            }
            factors++;
        }
        
        // If both are NULL, return 50 (neutral score) instead of 0
        if (factors == 0) {
            return BigDecimal.valueOf(50);
        }
        
        return score.min(BigDecimal.valueOf(100));
    }
    
    private BigDecimal calculateDebtScore(BigDecimal debtToEquity) {
        // If NULL, return 50 (neutral) instead of 0
        if (debtToEquity == null) return BigDecimal.valueOf(50);
        if (debtToEquity.compareTo(BigDecimal.valueOf(0.3)) < 0) return BigDecimal.valueOf(100);
        if (debtToEquity.compareTo(BigDecimal.valueOf(0.6)) < 0) return BigDecimal.valueOf(70);
        if (debtToEquity.compareTo(BigDecimal.valueOf(1.0)) < 0) return BigDecimal.valueOf(40);
        return BigDecimal.valueOf(20);
    }
    
    /**
     * Calculates Growth Score (0-100) based on Revenue Growth and EPS Growth (Year-over-Year).
     * 
     * <p>This score measures a company's ability to grow revenue and earnings over time.
     * Higher growth rates indicate stronger business momentum.
     * 
     * <p>Revenue Growth scoring:
     * <ul>
     *   <li>> 20%: 50 points (excellent growth)</li>
     *   <li>15-20%: 40 points (very good growth)</li>
     *   <li>10-15%: 30 points (good growth)</li>
     *   <li>5-10%: 20 points (moderate growth)</li>
     *   <li>< 5%: 10 points (slow growth)</li>
     * </ul>
     * 
     * <p>EPS Growth scoring (same scale as revenue growth):
     * <ul>
     *   <li>> 20%: 50 points (excellent earnings growth)</li>
     *   <li>15-20%: 40 points (very good earnings growth)</li>
     *   <li>10-15%: 30 points (good earnings growth)</li>
     *   <li>5-10%: 20 points (moderate earnings growth)</li>
     *   <li>< 5%: 10 points (slow earnings growth)</li>
     * </ul>
     * 
     * <p>The final score is the sum of both growth metrics (max 100 points).
     * 
     * @param revenueGrowthYoy Revenue growth year-over-year percentage (can be null)
     * @param epsGrowthYoy Earnings per share growth year-over-year percentage (can be null)
     * @return Growth score between 0-100, or 50 (neutral) if both growth metrics are null
     */
    private BigDecimal calculateGrowthScore(BigDecimal revenueGrowthYoy, BigDecimal epsGrowthYoy) {
        BigDecimal score = BigDecimal.ZERO;
        int factors = 0;
        
        // Revenue Growth: > 20% is excellent, 10-20% is good, 5-10% is fair
        if (revenueGrowthYoy != null && revenueGrowthYoy.compareTo(BigDecimal.ZERO) > 0) {
            if (revenueGrowthYoy.compareTo(BigDecimal.valueOf(20)) > 0) {
                score = score.add(BigDecimal.valueOf(50));
            } else if (revenueGrowthYoy.compareTo(BigDecimal.valueOf(15)) > 0) {
                score = score.add(BigDecimal.valueOf(40));
            } else if (revenueGrowthYoy.compareTo(BigDecimal.valueOf(10)) > 0) {
                score = score.add(BigDecimal.valueOf(30));
            } else if (revenueGrowthYoy.compareTo(BigDecimal.valueOf(5)) > 0) {
                score = score.add(BigDecimal.valueOf(20));
            } else {
                score = score.add(BigDecimal.valueOf(10));
            }
            factors++;
        }
        
        // EPS Growth: > 20% is excellent, 10-20% is good, 5-10% is fair
        if (epsGrowthYoy != null && epsGrowthYoy.compareTo(BigDecimal.ZERO) > 0) {
            if (epsGrowthYoy.compareTo(BigDecimal.valueOf(20)) > 0) {
                score = score.add(BigDecimal.valueOf(50));
            } else if (epsGrowthYoy.compareTo(BigDecimal.valueOf(15)) > 0) {
                score = score.add(BigDecimal.valueOf(40));
            } else if (epsGrowthYoy.compareTo(BigDecimal.valueOf(10)) > 0) {
                score = score.add(BigDecimal.valueOf(30));
            } else if (epsGrowthYoy.compareTo(BigDecimal.valueOf(5)) > 0) {
                score = score.add(BigDecimal.valueOf(20));
            } else {
                score = score.add(BigDecimal.valueOf(10));
            }
            factors++;
        }
        
        // If both are NULL, return 50 (neutral) instead of 0
        if (factors == 0) {
            return BigDecimal.valueOf(50);
        }
        
        return score.min(BigDecimal.valueOf(100));
    }
    
    private BigDecimal calculateGrowthRateScore(BigDecimal revenueGrowthYoy, BigDecimal epsGrowthYoy) {
        return calculateGrowthScore(revenueGrowthYoy, epsGrowthYoy);
    }
    
    private BigDecimal calculateRiskScore(BigDecimal debtToEquity, BigDecimal currentRatio) {
        BigDecimal score = BigDecimal.ZERO;
        // Higher debt = higher risk
        if (debtToEquity != null) {
            if (debtToEquity.compareTo(BigDecimal.valueOf(1.0)) > 0) {
                score = score.add(BigDecimal.valueOf(50));
            } else if (debtToEquity.compareTo(BigDecimal.valueOf(0.6)) > 0) {
                score = score.add(BigDecimal.valueOf(30));
            } else {
                score = score.add(BigDecimal.valueOf(10));
            }
        }
        // Low current ratio = higher risk
        if (currentRatio != null && currentRatio.compareTo(BigDecimal.valueOf(1.0)) < 0) {
            score = score.add(BigDecimal.valueOf(30));
        }
        // If both are NULL, return 50 (neutral risk) instead of 0
        if (debtToEquity == null && currentRatio == null) {
            return BigDecimal.valueOf(50);
        }
        return score.min(BigDecimal.valueOf(100));
    }
    
    private BigDecimal calculateProfitabilityScore(BigDecimal roe, BigDecimal roic) {
        BigDecimal score = BigDecimal.ZERO;
        if (roe != null && roe.compareTo(BigDecimal.ZERO) > 0) {
            // ROE > 15% is good
            if (roe.compareTo(BigDecimal.valueOf(20)) > 0) score = score.add(BigDecimal.valueOf(50));
            else if (roe.compareTo(BigDecimal.valueOf(15)) > 0) score = score.add(BigDecimal.valueOf(40));
            else if (roe.compareTo(BigDecimal.valueOf(10)) > 0) score = score.add(BigDecimal.valueOf(25));
        }
        if (roic != null && roic.compareTo(BigDecimal.ZERO) > 0) {
            // ROIC > 10% is good
            if (roic.compareTo(BigDecimal.valueOf(15)) > 0) score = score.add(BigDecimal.valueOf(50));
            else if (roic.compareTo(BigDecimal.valueOf(10)) > 0) score = score.add(BigDecimal.valueOf(40));
            else if (roic.compareTo(BigDecimal.valueOf(5)) > 0) score = score.add(BigDecimal.valueOf(25));
        }
        // If both are NULL, return 50 (neutral) instead of 0
        if (roe == null && roic == null) {
            return BigDecimal.valueOf(50);
        }
        return score.min(BigDecimal.valueOf(100));
    }
    
    /**
     * Calculates the Overall Score (0-100) by combining all category scores with weighted averages.
     * 
     * <p>Score weights:
     * <ul>
     *   <li>Valuation Score: 25% weight</li>
     *   <li>Financial Health Score: 30% weight</li>
     *   <li>Growth Score: 30% weight</li>
     *   <li>Risk Score: 15% weight (inverted - higher risk = lower contribution)</li>
     * </ul>
     * 
     * <p>The risk score is inverted because higher risk should decrease the overall score.
     * Formula: (100 - riskScore) is used to invert the risk contribution.
     * 
     * <p>The result is clamped between 0 and 100.
     * 
     * @param valuationScore Valuation score (0-100)
     * @param healthScore Financial health score (0-100)
     * @param growthScore Growth score (0-100)
     * @param riskScore Risk score (0-100, where higher = more risky)
     * @return Overall score between 0-100, rounded to 2 decimal places
     */
    private BigDecimal calculateOverallScore(BigDecimal valuationScore, BigDecimal healthScore, 
                                                   BigDecimal growthScore, BigDecimal riskScore) {
        BigDecimal invertedRiskScore = BigDecimal.valueOf(100).subtract(riskScore);
        BigDecimal weighted = valuationScore.multiply(BigDecimal.valueOf(0.25))
                .add(healthScore.multiply(BigDecimal.valueOf(0.30)))
                .add(growthScore.multiply(BigDecimal.valueOf(0.30)))
                .add(invertedRiskScore.multiply(BigDecimal.valueOf(0.15)));
        return weighted.setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
    }
    
    /**
     * Converts a numeric score (0-100) to a letter grade.
     * 
     * <p>Grading scale:
     * <ul>
     *   <li>A: 90-100 (Excellent)</li>
     *   <li>B: 80-89 (Very Good)</li>
     *   <li>C: 70-79 (Good)</li>
     *   <li>D: 60-69 (Fair)</li>
     *   <li>F: 0-59 (Poor)</li>
     * </ul>
     * 
     * @param score Numeric score between 0-100 (can be null)
     * @return Letter grade (A, B, C, D, F) or "N/A" if score is null
     */
    private String calculateGrade(BigDecimal score) {
        if (score == null) return "N/A";
        if (score.compareTo(BigDecimal.valueOf(90)) >= 0) return "A";
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) return "B";
        if (score.compareTo(BigDecimal.valueOf(70)) >= 0) return "C";
        if (score.compareTo(BigDecimal.valueOf(60)) >= 0) return "D";
        return "F";
    }
}
