package com.trueequity.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Verify that all tables were created successfully
 */
public class VerifySchema {
    
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/trueequity_market_data";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Vivek@025!";
    
    private static final String[] EXPECTED_TABLES = {
        "stocks", "stock_prices", "stock_prices_intraday", "stock_financials",
        "stock_scores", "technical_indicators", "market_data", "historical_snapshots",
        "stock_predictions", "strategy_backtests", "ingestion_log"
    };
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Verifying Database Schema");
        System.out.println("========================================");
        System.out.println();
        
        try {
            Class.forName("org.postgresql.Driver");
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                System.out.println("✅ Connected to database");
                System.out.println();
                
                int found = 0;
                int missing = 0;
                
                for (String table : EXPECTED_TABLES) {
                    if (tableExists(conn, table)) {
                        System.out.println("✅ " + table);
                        found++;
                    } else {
                        System.out.println("❌ " + table + " - MISSING");
                        missing++;
                    }
                }
                
                System.out.println();
                System.out.println("========================================");
                System.out.println("Results: " + found + " found, " + missing + " missing");
                System.out.println("========================================");
                
                if (missing == 0) {
                    System.out.println("✅ All tables created successfully!");
                } else {
                    System.out.println("⚠️  Some tables are missing. Please re-run the schema.");
                }
                
            } catch (SQLException e) {
                System.err.println("❌ Database error: " + e.getMessage());
                System.exit(1);
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL driver not found!");
            System.exit(1);
        }
    }
    
    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables " +
                     "WHERE table_schema = 'public' AND table_name = ?)";
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }
}

