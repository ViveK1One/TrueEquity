package com.trueequity.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Fast JDBC repository for stock prices
 * Uses batch inserts for maximum performance
 */
@Repository
public class JdbcStockPriceRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcStockPriceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Batch insert prices (fastest method)
     * Uses prepared statements for security
     * Validates data before inserting - prevents null or invalid values
     */
    public int[] batchInsertPrices(List<PriceData> prices) {
        // Filter out invalid prices before batch insert
        List<PriceData> validPrices = prices.stream()
            .filter(price -> {
                // Validate all required fields
                if (price.symbol() == null || price.symbol().isEmpty()) {
                    return false;
                }
                if (price.date() == null) {
                    return false;
                }
                if (price.open() == null || price.open().compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }
                if (price.high() == null || price.high().compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }
                if (price.low() == null || price.low().compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }
                if (price.close() == null || price.close().compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }
                if (price.volume() == null || price.volume() <= 0) {
                    return false;
                }
                // Validate high >= low and high >= close >= low
                if (price.high().compareTo(price.low()) < 0) {
                    return false;
                }
                if (price.close().compareTo(price.high()) > 0 || price.close().compareTo(price.low()) < 0) {
                    return false;
                }
                return true;
            })
            .toList();
        
        if (validPrices.isEmpty()) {
            return new int[0];
        }
        
        String sql = """
            INSERT INTO stock_prices (symbol, date, open, high, low, close, adjusted_close, volume, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (symbol, date) 
            DO UPDATE SET 
                open = EXCLUDED.open,
                high = EXCLUDED.high,
                low = EXCLUDED.low,
                close = EXCLUDED.close,
                adjusted_close = EXCLUDED.adjusted_close,
                volume = EXCLUDED.volume
            """;
        
        List<Object[]> batchArgs = validPrices.stream()
            .map(price -> new Object[]{
                price.symbol().toUpperCase(), // Always uppercase
                java.sql.Date.valueOf(price.date()),
                price.open(),
                price.high(),
                price.low(),
                price.close(),
                price.adjustedClose(), // Can be null
                price.volume()
            })
            .toList();
        
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * Get last price update timestamp for a symbol
     * Returns the most recent date from price records for this symbol
     */
    public java.time.LocalDateTime getLastPriceUpdate(String symbol) {
        String sql = "SELECT MAX(date) FROM stock_prices WHERE symbol = ?";
        try {
            java.sql.Date date = jdbcTemplate.queryForObject(sql, java.sql.Date.class, symbol.toUpperCase());
            return date != null ? date.toLocalDate().atStartOfDay() : null;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    /**
     * Get latest price for symbol
     */
    public BigDecimal getLatestPrice(String symbol) {
        String sql = "SELECT close FROM stock_prices WHERE symbol = ? ORDER BY date DESC LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, symbol);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get prices for a date range (for RSI calculation)
     */
    public List<PriceData> getPricesForDateRange(String symbol, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT symbol, date, open, high, low, close, adjusted_close, volume
            FROM stock_prices
            WHERE symbol = ? AND date >= ? AND date <= ?
            ORDER BY date ASC
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new PriceData(
                rs.getString("symbol"),
                rs.getDate("date").toLocalDate(),
                rs.getBigDecimal("open"),
                rs.getBigDecimal("high"),
                rs.getBigDecimal("low"),
                rs.getBigDecimal("close"),
                rs.getObject("adjusted_close", BigDecimal.class),
                rs.getLong("volume")
            );
        }, symbol.toUpperCase(), startDate, endDate);
    }

    /**
     * Price data record
     */
    public record PriceData(
        String symbol,
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal adjustedClose,
        Long volume
    ) {}
}

