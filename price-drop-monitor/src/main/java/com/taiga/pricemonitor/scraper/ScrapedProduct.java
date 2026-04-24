package com.taiga.pricemonitor.scraper;

public class ScrapedProduct {
    private final String url;
    private final String name;
    private final double price;

    public ScrapedProduct(String url, String name, double price) {
        this.url = url;
        this.name = name;
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}
