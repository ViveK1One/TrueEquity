package com.trueequity.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Fast JDBC repository for stocks
 * Uses prepared statements for security and performance
 */
@Repository
public class JdbcStockRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcStockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Insert or update stock (using prepared statement for security)
     * Validates data before inserting - prevents null values for required fields
     */
    public void upsertStock(String symbol, String name, String exchange, String sector, String industry, Long marketCap) {
        // Validate required fields - throw exception if invalid
        if (symbol == null || symbol.isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (name == null || name.isEmpty() || name.equals("null")) {
            throw new IllegalArgumentException("Name cannot be null or empty for symbol: " + symbol);
        }
        if (exchange == null || exchange.isEmpty()) {
            exchange = "NASDAQ"; // Default value
        }
        
        String sql = """
            INSERT INTO stocks (symbol, name, exchange, sector, industry, market_cap, is_active, added_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (symbol) 
            DO UPDATE SET 
                name = EXCLUDED.name,
                sector = EXCLUDED.sector,
                industry = EXCLUDED.industry,
                market_cap = EXCLUDED.market_cap,
                updated_at = CURRENT_TIMESTAMP
            """;
        
        // Only insert non-null values for optional fields
        jdbcTemplate.update(sql, 
            symbol.toUpperCase(), // Always uppercase
            name, // Required, already validated
            exchange, // Required, has default
            (sector != null && !sector.isEmpty() && !sector.equals("null")) ? sector : null,
            (industry != null && !industry.isEmpty() && !industry.equals("null")) ? industry : null,
            (marketCap != null && marketCap > 0) ? marketCap : null
        );
    }

    /**
     * Check if stock exists
     */
    public boolean exists(String symbol) {
        String sql = "SELECT EXISTS(SELECT 1 FROM stocks WHERE symbol = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, symbol);
        return exists != null && exists;
    }

    /**
     * Get all active stock symbols
     */
    public List<String> getAllActiveSymbols() {
        String sql = "SELECT symbol FROM stocks WHERE is_active = true";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    
    /**
     * Get last update timestamp for a stock
     * Returns null if stock doesn't exist
     */
    public java.time.LocalDateTime getLastUpdated(String symbol) {
        String sql = "SELECT updated_at FROM stocks WHERE symbol = ?";
        try {
            java.sql.Timestamp timestamp = jdbcTemplate.queryForObject(sql, java.sql.Timestamp.class, symbol.toUpperCase());
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Returns true if stock has static data (sector, industry, exchange, name) already filled.
     * Used to avoid wasting API calls on profile when only price/ratios need updating.
     */
    public boolean hasStaticData(String symbol) {
        String sql = "SELECT (sector IS NOT NULL AND TRIM(sector) != '') AND (industry IS NOT NULL AND TRIM(industry) != '') " +
                     "AND (exchange IS NOT NULL AND TRIM(exchange) != '') AND (name IS NOT NULL AND TRIM(name) != '') " +
                     "FROM stocks WHERE symbol = ?";
        try {
            Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, symbol.toUpperCase());
            return Boolean.TRUE.equals(result);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * Update market cap for an existing stock (market cap changes with price, so needs regular updates)
     */
    public void updateMarketCap(String symbol, Long marketCap) {
        if (marketCap == null || marketCap <= 0) {
            return; // Don't update with invalid values
        }
        String sql = "UPDATE stocks SET market_cap = ?, updated_at = CURRENT_TIMESTAMP WHERE symbol = ?";
        jdbcTemplate.update(sql, marketCap, symbol.toUpperCase());
    }
}

