package com.taiga.pricemonitor;

import com.taiga.pricemonitor.comparison.PriceComparator;
import com.taiga.pricemonitor.comparison.PriceDropResult;
import com.taiga.pricemonitor.config.AppConfig;
import com.taiga.pricemonitor.config.ConfigLoader;
import com.taiga.pricemonitor.config.ProductConfig;
import com.taiga.pricemonitor.db.DatabaseService;
import com.taiga.pricemonitor.scraper.AmazonScraper;
import com.taiga.pricemonitor.scraper.ScrapedProduct;
import com.taiga.pricemonitor.scraper.ScraperException;

public class App {
    public static void main(String[] args) {
        AppConfig config = ConfigLoader.load("config.yaml");
        DatabaseService db = new DatabaseService("prices.db");
        AmazonScraper scraper = new AmazonScraper();
        PriceComparator comparator = new PriceComparator(config.getPriceDropThresholdPercent());

        for (ProductConfig product : config.getProducts()) {
            try {
                Double previousPrice = db.getLastPrice(product.getUrl());
                ScrapedProduct scraped = scraper.scrape(product.getUrl());
                PriceDropResult result = comparator.compare(previousPrice, scraped.getPrice());

                db.savePrice(product.getUrl(), scraped.getName(), scraped.getPrice());

                if (result.isDrop()) {
                    System.out.println("[PRICE DROP] " + scraped.getName() +
                        " | $" + result.getPreviousPrice() + " -> $" + result.getCurrentPrice() +
                        " (" + String.format("%.2f", result.getDropPercent()) + "% off)");
                } else {
                    System.out.println("[OK] " + scraped.getName() + " -> $" + scraped.getPrice());
                }

            } catch (ScraperException e) {
                System.out.println("[FAIL] " + product.getName() + ": " + e.getMessage());
            }
        }
    }
}