package com.trueequity.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fast JDBC repository for stock scores
 * Uses prepared statements for security
 */
@Repository
public class JdbcStockScoreRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcStockScoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Insert or update score (upsert - keeps latest score per symbol)
     */
    public void upsertScore(ScoreData data) {
        // Delete old scores for this symbol (keep only latest)
        String deleteSql = "DELETE FROM stock_scores WHERE symbol = ?";
        jdbcTemplate.update(deleteSql, data.symbol().toUpperCase());
        
        // Insert new score
        String sql = """
            INSERT INTO stock_scores (
                id, symbol, calculated_at,
                valuation_category, valuation_score,
                health_score, health_grade,
                growth_score, growth_grade,
                risk_score, risk_grade,
                overall_score, overall_grade,
                pe_score, peg_score, debt_score, profitability_score,
                growth_rate_score, volatility_score
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            data.id(),
            data.symbol().toUpperCase(),
            data.calculatedAt(),
            data.valuationCategory(),
            data.valuationScore(),
            data.healthScore(),
            data.healthGrade(),
            data.growthScore(),
            data.growthGrade(),
            data.riskScore(),
            data.riskGrade(),
            data.overallScore(),
            data.overallGrade(),
            data.peScore(),
            data.pegScore(),
            data.debtScore(),
            data.profitabilityScore(),
            data.growthRateScore(),
            data.volatilityScore()
        );
    }
    
    /**
     * Get last calculation timestamp for a stock's score
     * Returns null if no score exists
     */
    public java.time.LocalDateTime getLastCalculated(String symbol) {
        String sql = "SELECT MAX(calculated_at) FROM stock_scores WHERE symbol = ?";
        try {
            java.sql.Timestamp timestamp = jdbcTemplate.queryForObject(sql, java.sql.Timestamp.class, symbol.toUpperCase());
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Score data record matching database schema
     */
    public record ScoreData(
        UUID id,
        String symbol,
        LocalDateTime calculatedAt,
        String valuationCategory,
        BigDecimal valuationScore,
        BigDecimal healthScore,
        String healthGrade,
        BigDecimal growthScore,
        String growthGrade,
        BigDecimal riskScore,
        String riskGrade,
        BigDecimal overallScore,
        String overallGrade,
        BigDecimal peScore,
        BigDecimal pegScore,
        BigDecimal debtScore,
        BigDecimal profitabilityScore,
        BigDecimal growthRateScore,
        BigDecimal volatilityScore
    ) {}
}

