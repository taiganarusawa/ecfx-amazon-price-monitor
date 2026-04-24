package com.taiga.pricemonitor.scraper;

public class ScraperException extends Exception {
    public ScraperException(String message) {
        super(message);
    }

    public ScraperException(String message, Throwable cause) {
        super(message, cause);
    }
}