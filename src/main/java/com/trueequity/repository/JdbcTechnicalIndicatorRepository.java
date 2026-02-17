package com.trueequity.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Repository for technical indicators (RSI, MACD, etc.)
 */
@Repository
public class JdbcTechnicalIndicatorRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTechnicalIndicatorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Upsert RSI value for a symbol and date
     */
    public void upsertRSI(String symbol, LocalDate date, BigDecimal rsi) {
        String sql = """
            INSERT INTO technical_indicators (id, symbol, date, rsi, created_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (symbol, date)
            DO UPDATE SET rsi = EXCLUDED.rsi
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID(),
            symbol.toUpperCase(),
            date,
            rsi
        );
    }

    /**
     * Get latest RSI for a symbol
     */
    public BigDecimal getLatestRSI(String symbol) {
        String sql = "SELECT rsi FROM technical_indicators WHERE symbol = ? ORDER BY date DESC LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, symbol.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get last RSI update date for a symbol
     */
    public java.time.LocalDateTime getLastRSIUpdate(String symbol) {
        String sql = "SELECT MAX(date) FROM technical_indicators WHERE symbol = ? AND rsi IS NOT NULL";
        try {
            java.sql.Date date = jdbcTemplate.queryForObject(sql, java.sql.Date.class, symbol.toUpperCase());
            return date != null ? date.toLocalDate().atStartOfDay() : null;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Store hourly RSI (stores in same table with current date, can be retrieved separately)
     * Note: This stores hourly RSI with today's date for easy retrieval
     */
    public void upsertHourlyRSI(String symbol, java.math.BigDecimal hourlyRSI) {
        String sql = """
            INSERT INTO technical_indicators (id, symbol, date, rsi, created_at)
            VALUES (?, ?, CURRENT_DATE, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (symbol, date)
            DO UPDATE SET rsi = EXCLUDED.rsi
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID(),
            symbol.toUpperCase(),
            hourlyRSI
        );
    }
}

