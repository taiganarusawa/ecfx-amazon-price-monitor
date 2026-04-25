package com.taiga.pricemonitor.service;

import com.taiga.pricemonitor.comparison.PriceComparator;
import com.taiga.pricemonitor.comparison.PriceDropResult;
import com.taiga.pricemonitor.config.ProductConfig;
import com.taiga.pricemonitor.db.DatabaseService;
import com.taiga.pricemonitor.scraper.AmazonScraper;
import com.taiga.pricemonitor.scraper.ScrapedProduct;
import com.taiga.pricemonitor.scraper.ScraperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceCheckService {

    private static final Logger logger = LoggerFactory.getLogger(PriceCheckService.class);

    private final AmazonScraper scraper;
    private final DatabaseService db;
    private final PriceComparator comparator;

    public PriceCheckService(AmazonScraper scraper, DatabaseService db, PriceComparator comparator) {
        this.scraper = scraper;
        this.db = db;
        this.comparator = comparator;
    }

    // Runs the full check pipeline for a single product
    // Errors are logged but never thrown
    public void checkProduct(ProductConfig product) {
        try {
            logger.info("Checking product: {}", product.getName());
            Double previousPrice = db.getLastPrice(product.getUrl());
            ScrapedProduct scraped = scraper.scrape(product.getUrl());
            PriceDropResult result = comparator.compare(previousPrice, scraped.getPrice());

            db.savePrice(product.getUrl(), scraped.getName(), scraped.getPrice());

            if (result.isDrop()) {
                logger.warn("Price drop detected for {}: previous={} current={} dropPercent={:.2f}%",
                        scraped.getName(), result.getPreviousPrice(), result.getCurrentPrice(), result.getDropPercent());
            }
        } catch (ScraperException e) {
            logger.error("Error occurred while checking product: {}", product.getName(), e);
        } catch (Exception e) {
            logger.error("Unexpected error occurred while checking product: {}", product.getName(), e);
        }
    }
}
