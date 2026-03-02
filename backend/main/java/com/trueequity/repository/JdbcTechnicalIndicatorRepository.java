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

    /** Timeframe values: 1h (1 Hour), 30m (1 Month), 2h (6 Months), 1d (1 Year) */
    public static final String TIMEFRAME_1H = "1h";
    public static final String TIMEFRAME_30M = "30m";
    public static final String TIMEFRAME_2H = "2h";
    public static final String TIMEFRAME_1D = "1d";

    /**
     * Upsert RSI value for a symbol, date and timeframe.
     * @param timeframe "1h", "30m", "2h", or "1d"
     */
    public void upsertRSI(String symbol, LocalDate date, String timeframe, BigDecimal rsi) {
        String sql = """
            INSERT INTO technical_indicators (id, symbol, date, timeframe, rsi, created_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (symbol, date, timeframe)
            DO UPDATE SET rsi = EXCLUDED.rsi
            """;
        jdbcTemplate.update(sql,
            UUID.randomUUID(),
            symbol.toUpperCase(),
            date,
            (timeframe != null ? timeframe : TIMEFRAME_1D),
            rsi
        );
    }

    /**
     * Upsert RSI value for a symbol and date (daily/1-year timeframe).
     */
    public void upsertRSI(String symbol, LocalDate date, BigDecimal rsi) {
        upsertRSI(symbol, date, TIMEFRAME_1D, rsi);
    }

    /**
     * Get latest RSI for a symbol and timeframe.
     * @param timeframe "1h", "30m", "2h", or "1d"
     */
    public BigDecimal getLatestRSIForTimeframe(String symbol, String timeframe) {
        String sql = "SELECT rsi FROM technical_indicators WHERE symbol = ? AND timeframe = ? ORDER BY date DESC LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, symbol.toUpperCase(), (timeframe != null ? timeframe : TIMEFRAME_1D));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get latest RSI for a symbol (any timeframe; prefers latest date).
     */
    public BigDecimal getLatestRSI(String symbol) {
        return getLatestRSIForTimeframe(symbol, TIMEFRAME_1D);
    }

    /**
     * Get last RSI update date for a symbol (for default 1d timeframe).
     */
    public java.time.LocalDateTime getLastRSIUpdate(String symbol) {
        String sql = "SELECT MAX(date) FROM technical_indicators WHERE symbol = ? AND timeframe = ? AND rsi IS NOT NULL";
        try {
            java.sql.Date date = jdbcTemplate.queryForObject(sql, java.sql.Date.class, symbol.toUpperCase(), TIMEFRAME_1D);
            return date != null ? date.toLocalDate().atStartOfDay() : null;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Store hourly RSI (1h timeframe) with current date.
     */
    public void upsertHourlyRSI(String symbol, java.math.BigDecimal hourlyRSI) {
        upsertRSI(symbol, LocalDate.now(), TIMEFRAME_1H, hourlyRSI);
    }
}

