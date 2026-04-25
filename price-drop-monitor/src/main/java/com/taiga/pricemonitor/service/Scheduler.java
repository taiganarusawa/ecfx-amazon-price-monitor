package com.taiga.pricemonitor.service;

import com.taiga.pricemonitor.config.AppConfig;
import com.taiga.pricemonitor.config.ProductConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    private final AppConfig config;
    private final PriceCheckService checkService;
    private final ScheduledExecutorService executor;

    public Scheduler(AppConfig config, PriceCheckService checkService) {
        this.config = config;
        this.checkService = checkService;
        this.executor = Executors.newScheduledThreadPool(config.getProducts().size() + 1);
    }

    public void start() {
        long intervalMinutes = config.getCheckIntervalMinutes();
        logger.info("Starting scheduler with interval of {} minutes", intervalMinutes);

        executor.scheduleAtFixedRate(
            this::runCheckCycle,
            0, 
            intervalMinutes, 
            TimeUnit.MINUTES
        );
    }

    private void runCheckCycle() {
        logger.info("Running price check cycle for {} products", config.getProducts().size());
        for (ProductConfig product : config.getProducts()) {
            executor.submit(() -> checkService.checkProduct(product));
        }
    }

    public void stop() {
        logger.info("Stopping scheduler...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Forcing shutdown of scheduler...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Scheduler shutdown interrupted", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
