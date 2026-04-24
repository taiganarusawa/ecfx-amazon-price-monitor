package com.taiga.pricemonitor.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private final String dbUrl;

    public DatabaseService(String dbPath) {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initializeDatabase();
    }

    private void initializeDatabase() {
        String sql = """
            CREATE TABLE IF NOT EXISTS price_history  (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                product_url TEXT NOT NULL,
                product_name TEXT NOT NULL,
                price REAL NOT NULL,
                checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
            Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public void savePrice(String productUrl, String productName, double price) {
        String sql = "INSERT INTO price_history (product_url, product_name, price) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productUrl);
            pstmt.setString(2, productName);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();
            logger.info("Price saved for {}: ${}", productName, price);
        } catch (SQLException e) {
            logger.error("Failed to save price for {}: {}", productName, e.getMessage());
        }
    }

    public Double getLastPrice(String productURL) {
        String sql = """
            SELECT price FROM price_history
            WHERE product_url = ?
            ORDER BY checked_at DESC
            LIMIT 1
            """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productURL);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        } catch (SQLException e) {
            logger.error("Failed to get last price for {}: {}", productURL, e.getMessage());
        }
        return null;
    }

    public List<PriceRecord> getPriceHistory(String productUrl) {
        String sql = """
            SELECT product_name, price, checked_at FROM price_history
            WHERE product_url = ?
            ORDER BY checked_at ASC
            """;
        List<PriceRecord> history = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productUrl);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                history.add(new PriceRecord(
                    rs.getString("product_name"),
                    rs.getDouble("price"),
                    rs.getString("checked_at")
                ));
            }
        } catch (SQLException e) {
            logger.error("Failed to get price history for {}: {}", productUrl, e.getMessage());
        }
        return history;
    }
}