package com.taiga.pricemonitor;

import com.taiga.pricemonitor.comparison.PriceComparator;
import com.taiga.pricemonitor.config.AppConfig;
import com.taiga.pricemonitor.config.ConfigLoader;
import com.taiga.pricemonitor.db.DatabaseService;
import com.taiga.pricemonitor.scraper.AmazonScraper;
import com.taiga.pricemonitor.service.PriceCheckService;
import com.taiga.pricemonitor.service.Scheduler;

public class App {
    public static void main(String[] args) {
        AppConfig config = ConfigLoader.load("config.yaml");
        DatabaseService db = new DatabaseService("prices.db");
        AmazonScraper scraper = new AmazonScraper();
        PriceComparator comparator = new PriceComparator(config.getPriceDropThresholdPercent());
        PriceCheckService checkService = new PriceCheckService(scraper, db, comparator);
        Scheduler scheduler = new Scheduler(config, checkService);

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::stop));

        scheduler.start();

        // Keep main thread alive so the scheduler can keep running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}