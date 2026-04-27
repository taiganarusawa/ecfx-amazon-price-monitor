package com.taiga.pricemonitor.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseServiceTest {

    private static final String TEST_DB = "test-prices.db";
    private DatabaseService db;

    @BeforeEach
    public void setUp() {
        // Delete any existing test DB so each test starts fresh
        new File(TEST_DB).delete();
        db = new DatabaseService(TEST_DB);
    }

    @AfterEach
    public void tearDown() {
        new File(TEST_DB).delete();
    }

    @Test
    public void savesAndRetrievesPrice() {
        String url = "https://amazon.com/dp/TEST123";
        db.savePrice(url, "Test Product", 99.99);

        Double lastPrice = db.getLastPrice(url);
        assertNotNull(lastPrice);
        assertEquals(99.99, lastPrice, 0.001);
    }

    @Test
    public void returnsNullForUnknownProduct() {
        Double lastPrice = db.getLastPrice("https://amazon.com/dp/NONEXISTENT");
        assertNull(lastPrice);
    }

    @Test
    public void getLastPriceReturnsMostRecent() {
        String url = "https://amazon.com/dp/TEST456";
        db.savePrice(url, "Test", 50.00);
        db.savePrice(url, "Test", 45.00);
        db.savePrice(url, "Test", 40.00);

        Double lastPrice = db.getLastPrice(url);
        assertEquals(40.00, lastPrice, 0.001);
    }

    @Test
    public void priceHistoryReturnsAllRecordsInOrder() {
        String url = "https://amazon.com/dp/TEST789";
        db.savePrice(url, "Test", 30.00);
        db.savePrice(url, "Test", 25.00);

        List<PriceRecord> history = db.getPriceHistory(url);
        assertEquals(2, history.size());
        assertEquals(30.00, history.get(0).getPrice(), 0.001);
        assertEquals(25.00, history.get(1).getPrice(), 0.001);
    }
}