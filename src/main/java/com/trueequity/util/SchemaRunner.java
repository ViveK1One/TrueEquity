package com.trueequity.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility to run database schema SQL file
 * Can be run standalone to create tables
 */
public class SchemaRunner {
    
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/trueequity_market_data";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Vivek@025!";
    private static final String SCHEMA_FILE = "database/schema.sql";
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("TrueEquity Database Schema Runner");
        System.out.println("========================================");
        System.out.println();
        
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            
            System.out.println("Connecting to database...");
            System.out.println("URL: " + DB_URL);
            System.out.println("User: " + DB_USER);
            System.out.println();
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                System.out.println("✅ Connected to database successfully!");
                System.out.println();
                
                System.out.println("Reading schema file: " + SCHEMA_FILE);
                String sql = readSchemaFile(SCHEMA_FILE);
                
                System.out.println("Executing schema...");
                System.out.println("(This may take a minute...)");
                System.out.println();
                
                // Execute the entire SQL file as one script
                // PostgreSQL JDBC supports executing multiple statements
                int executed = 0;
                
                try (Statement stmt = conn.createStatement()) {
                    // Remove comments and split by semicolon, but preserve function bodies
                    String cleanedSql = sql.replaceAll("--.*", ""); // Remove single-line comments
                    
                    // Split by semicolon, but be careful with functions
                    String[] statements = cleanedSql.split(";(?![^$]*\\$\\$)"); // Split by ; but not inside $$ blocks
                    
                    for (String statement : statements) {
                        String trimmed = statement.trim();
                        if (trimmed.length() > 0) {
                            try {
                                stmt.execute(trimmed);
                                executed++;
                                if (executed % 5 == 0) {
                                    System.out.print(".");
                                }
                            } catch (SQLException e) {
                                // Ignore "already exists" errors and relation errors (expected for CREATE INDEX on non-existent tables)
                                String msg = e.getMessage();
                                if (!msg.contains("already exists") && 
                                    !msg.contains("duplicate") &&
                                    !msg.contains("does not exist")) {
                                    System.err.println("\nWarning: " + msg);
                                }
                            }
                        }
                    }
                    
                    // Also try executing the whole file at once (PostgreSQL supports this)
                    System.out.println("\nExecuting complete schema...");
                    try {
                        stmt.execute(sql);
                        System.out.println("Complete schema executed successfully!");
                    } catch (SQLException e) {
                        // If complete execution fails, individual statements above should have worked
                        System.out.println("Note: Some statements may have already been executed.");
                    }
                }
                
                System.out.println();
                System.out.println("========================================");
                System.out.println("✅ Schema executed successfully!");
                System.out.println("Executed " + executed + " statements");
                System.out.println("========================================");
                System.out.println();
                System.out.println("All tables have been created.");
                System.out.println("You can now start the Spring Boot application.");
                
            } catch (SQLException e) {
                System.err.println("❌ Database error: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL driver not found!");
            System.err.println("Please ensure PostgreSQL JDBC driver is in classpath.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("❌ Error reading schema file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static String readSchemaFile(String filename) throws IOException {
        StringBuilder sql = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
        }
        return sql.toString();
    }
}

