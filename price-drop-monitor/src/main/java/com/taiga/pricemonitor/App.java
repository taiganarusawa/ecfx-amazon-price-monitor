package com.taiga.pricemonitor;

import com.taiga.pricemonitor.config.AppConfig;
import com.taiga.pricemonitor.config.ConfigLoader;
import com.taiga.pricemonitor.config.ProductConfig;
import com.taiga.pricemonitor.scraper.AmazonScraper;
import com.taiga.pricemonitor.scraper.ScrapedProduct;
import com.taiga.pricemonitor.scraper.ScraperException;

public class App {
    public static void main(String[] args) {
        AppConfig config = ConfigLoader.load("config.yaml");
        AmazonScraper scraper = new AmazonScraper();

        for (ProductConfig product : config.getProducts()) {
            try {
                ScrapedProduct scraped = scraper.scrape(product.getUrl());
                System.out.println(scraped.getName() + " -> $" + scraped.getPrice());
            } catch (ScraperException e) {
                System.out.println("Failed to scrape " + product.getName() + ": " + e.getMessage());
            }
        }
    }
}