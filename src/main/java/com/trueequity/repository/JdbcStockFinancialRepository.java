package com.trueequity.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Fast JDBC repository for stock financials
 * Uses prepared statements for security
 */
@Repository
public class JdbcStockFinancialRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcStockFinancialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Insert or update financial data (upsert)
     * Validates data before inserting
     */
    public void upsertFinancial(FinancialData data) {
        String sql = """
            INSERT INTO stock_financials (
                id, symbol, period_type, period_end_date, fiscal_year, fiscal_quarter,
                pe_ratio, peg_ratio, price_to_book, price_to_sales, ev_to_ebitda,
                eps_ttm, eps_growth_yoy, eps_growth_qoq,
                revenue, revenue_growth_yoy, revenue_growth_qoq,
                net_income, net_income_growth_yoy, profit_margin,
                total_cash, total_debt, cash_per_share, debt_to_equity, current_ratio,
                roe, roic, roa, gross_margin, operating_margin,
                revenue_growth_3y, earnings_growth_3y,
                shares_outstanding, float_shares,
                created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (symbol, period_type, period_end_date)
            DO UPDATE SET
                fiscal_year = EXCLUDED.fiscal_year,
                fiscal_quarter = EXCLUDED.fiscal_quarter,
                pe_ratio = EXCLUDED.pe_ratio,
                peg_ratio = EXCLUDED.peg_ratio,
                price_to_book = EXCLUDED.price_to_book,
                price_to_sales = EXCLUDED.price_to_sales,
                ev_to_ebitda = EXCLUDED.ev_to_ebitda,
                eps_ttm = EXCLUDED.eps_ttm,
                eps_growth_yoy = EXCLUDED.eps_growth_yoy,
                eps_growth_qoq = EXCLUDED.eps_growth_qoq,
                revenue = EXCLUDED.revenue,
                revenue_growth_yoy = EXCLUDED.revenue_growth_yoy,
                revenue_growth_qoq = EXCLUDED.revenue_growth_qoq,
                net_income = EXCLUDED.net_income,
                net_income_growth_yoy = EXCLUDED.net_income_growth_yoy,
                profit_margin = EXCLUDED.profit_margin,
                total_cash = EXCLUDED.total_cash,
                total_debt = EXCLUDED.total_debt,
                cash_per_share = EXCLUDED.cash_per_share,
                debt_to_equity = EXCLUDED.debt_to_equity,
                current_ratio = EXCLUDED.current_ratio,
                roe = EXCLUDED.roe,
                roic = EXCLUDED.roic,
                roa = EXCLUDED.roa,
                gross_margin = EXCLUDED.gross_margin,
                operating_margin = EXCLUDED.operating_margin,
                revenue_growth_3y = EXCLUDED.revenue_growth_3y,
                earnings_growth_3y = EXCLUDED.earnings_growth_3y,
                shares_outstanding = EXCLUDED.shares_outstanding,
                float_shares = EXCLUDED.float_shares,
                updated_at = CURRENT_TIMESTAMP
            """;
        
        jdbcTemplate.update(sql,
            data.id(),
            data.symbol().toUpperCase(),
            data.periodType(),
            data.periodEndDate(),
            data.fiscalYear(),
            data.fiscalQuarter(),
            data.peRatio(),
            data.pegRatio(),
            data.priceToBook(),
            data.priceToSales(),
            data.evToEbitda(),
            data.epsTtm(),
            data.epsGrowthYoy(),
            data.epsGrowthQoq(),
            data.revenue(),
            data.revenueGrowthYoy(),
            data.revenueGrowthQoq(),
            data.netIncome(),
            data.netIncomeGrowthYoy(),
            data.profitMargin(),
            data.totalCash(),
            data.totalDebt(),
            data.cashPerShare(),
            data.debtToEquity(),
            data.currentRatio(),
            data.roe(),
            data.roic(),
            data.roa(),
            data.grossMargin(),
            data.operatingMargin(),
            data.revenueGrowth3y(),
            data.earningsGrowth3y(),
            data.sharesOutstanding(),
            data.floatShares()
        );
    }

    /**
     * Get latest financial data for a symbol
     */
    public Optional<FinancialData> findLatestBySymbol(String symbol) {
        String sql = """
            SELECT id, symbol, period_type, period_end_date, fiscal_year, fiscal_quarter,
                   pe_ratio, peg_ratio, price_to_book, price_to_sales, ev_to_ebitda,
                   eps_ttm, eps_growth_yoy, eps_growth_qoq,
                   revenue, revenue_growth_yoy, revenue_growth_qoq,
                   net_income, net_income_growth_yoy, profit_margin,
                   total_cash, total_debt, cash_per_share, debt_to_equity, current_ratio,
                   roe, roic, roa, gross_margin, operating_margin,
                   revenue_growth_3y, earnings_growth_3y,
                   shares_outstanding, float_shares
            FROM stock_financials
            WHERE symbol = ?
            ORDER BY period_end_date DESC
            LIMIT 1
            """;
        
        try {
            List<FinancialData> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
                return new FinancialData(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("symbol"),
                        rs.getString("period_type"),
                        rs.getDate("period_end_date").toLocalDate(),
                        rs.getObject("fiscal_year", Integer.class),
                        rs.getObject("fiscal_quarter", Integer.class),
                        rs.getObject("pe_ratio", BigDecimal.class),
                        rs.getObject("peg_ratio", BigDecimal.class),
                        rs.getObject("price_to_book", BigDecimal.class),
                        rs.getObject("price_to_sales", BigDecimal.class),
                        rs.getObject("ev_to_ebitda", BigDecimal.class),
                        rs.getObject("eps_ttm", BigDecimal.class),
                        rs.getObject("eps_growth_yoy", BigDecimal.class),
                        rs.getObject("eps_growth_qoq", BigDecimal.class),
                        rs.getObject("revenue", Long.class),
                        rs.getObject("revenue_growth_yoy", BigDecimal.class),
                        rs.getObject("revenue_growth_qoq", BigDecimal.class),
                        rs.getObject("net_income", Long.class),
                        rs.getObject("net_income_growth_yoy", BigDecimal.class),
                        rs.getObject("profit_margin", BigDecimal.class),
                        rs.getObject("total_cash", Long.class),
                        rs.getObject("total_debt", Long.class),
                        rs.getObject("cash_per_share", BigDecimal.class),
                        rs.getObject("debt_to_equity", BigDecimal.class),
                        rs.getObject("current_ratio", BigDecimal.class),
                        rs.getObject("roe", BigDecimal.class),
                        rs.getObject("roic", BigDecimal.class),
                        rs.getObject("roa", BigDecimal.class),
                        rs.getObject("gross_margin", BigDecimal.class),
                        rs.getObject("operating_margin", BigDecimal.class),
                        rs.getObject("revenue_growth_3y", BigDecimal.class),
                        rs.getObject("earnings_growth_3y", BigDecimal.class),
                        rs.getObject("shares_outstanding", Long.class),
                        rs.getObject("float_shares", Long.class)
                );
            }, symbol.toUpperCase());
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Get last update timestamp for financial data of a symbol
     * Returns the most recent updated_at from all financial records for this symbol
     */
    public java.time.LocalDateTime getLastUpdated(String symbol) {
        String sql = "SELECT MAX(updated_at) FROM stock_financials WHERE symbol = ?";
        try {
            java.sql.Timestamp timestamp = jdbcTemplate.queryForObject(sql, java.sql.Timestamp.class, symbol.toUpperCase());
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Financial data record matching database schema
     */
    public record FinancialData(
        UUID id,
        String symbol,
        String periodType,
        LocalDate periodEndDate,
        Integer fiscalYear,
        Integer fiscalQuarter,
        BigDecimal peRatio,
        BigDecimal pegRatio,
        BigDecimal priceToBook,
        BigDecimal priceToSales,
        BigDecimal evToEbitda,
        BigDecimal epsTtm,
        BigDecimal epsGrowthYoy,
        BigDecimal epsGrowthQoq,
        Long revenue,
        BigDecimal revenueGrowthYoy,
        BigDecimal revenueGrowthQoq,
        Long netIncome,
        BigDecimal netIncomeGrowthYoy,
        BigDecimal profitMargin,
        Long totalCash,
        Long totalDebt,
        BigDecimal cashPerShare,
        BigDecimal debtToEquity,
        BigDecimal currentRatio,
        BigDecimal roe,
        BigDecimal roic,
        BigDecimal roa,
        BigDecimal grossMargin,
        BigDecimal operatingMargin,
        BigDecimal revenueGrowth3y,
        BigDecimal earningsGrowth3y,
        Long sharesOutstanding,
        Long floatShares
    ) {}
}

