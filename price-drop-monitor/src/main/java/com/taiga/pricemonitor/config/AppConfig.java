package com.taiga.pricemonitor.config;

import java.util.List;

public class AppConfig {
    private List<ProductConfig> products;
    private int checkIntervalMinutes;
    private double priceDropThresholdPercent;
    private NotificationConfig notification;
    private boolean testMode;
    private int dashboardPort;
    private int scraperTimeoutSeconds;

    public List<ProductConfig> getProducts() {
        return products;
    }

    public int getCheckIntervalMinutes() {
        return checkIntervalMinutes;
    }

    public double getPriceDropThresholdPercent() {
        return priceDropThresholdPercent;
    }

    public NotificationConfig getNotification() {
        return notification;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public int getDashboardPort() {
        return dashboardPort;
    }

    public int getScraperTimeoutSeconds() {
        return scraperTimeoutSeconds;
    }

    public void setProducts(List<ProductConfig> products) {
        this.products = products;
    }

    public void setCheckIntervalMinutes(int checkIntervalMinutes) {
        this.checkIntervalMinutes = checkIntervalMinutes;
    }

    public void setPriceDropThresholdPercent(double priceDropThresholdPercent) {
        this.priceDropThresholdPercent = priceDropThresholdPercent;
    }

    public void setNotification(NotificationConfig notification) {
        this.notification = notification;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public void setDashboardPort(int dashboardPort) {
        this.dashboardPort = dashboardPort;
    }

    public void setScraperTimeoutSeconds(int scraperTimeoutSeconds) {
        this.scraperTimeoutSeconds = scraperTimeoutSeconds;
    }
}
