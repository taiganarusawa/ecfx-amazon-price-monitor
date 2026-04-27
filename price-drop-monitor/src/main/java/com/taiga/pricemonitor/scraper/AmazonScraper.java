package com.taiga.pricemonitor.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AmazonScraper {
    private static final Logger logger = LoggerFactory.getLogger(AmazonScraper.class);

    private static final String USER_AGENT = 
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    private final int timeoutMs;

    public AmazonScraper(int timeoutSeconds) {
        this.timeoutMs = timeoutSeconds * 1000;
    }

    public ScrapedProduct scrape(String url) throws ScraperException {
        try {
            Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept-Language", "en-US,en;q=0.9")
                .timeout(timeoutMs)
                .get();

            String name = extractName(doc);
            double price = extractPrice(doc);

            return new ScrapedProduct(url, name, price);
        } catch (IOException e) {
            throw new ScraperException("Failed to fetch page: " + url, e);
        }
    }

    private String extractName(Document doc) throws ScraperException {
        Element titleElement = doc.selectFirst("#productTitle");
        if (titleElement == null) {
            throw new ScraperException("Could not find product title");
        }
        return titleElement.text().trim();
    }

    private double extractPrice(Document doc) throws ScraperException {
        String[] selectors = {
            "span.a-price > span.a-offscreen",
            "span.a-price-whole",
            "#priceblock_ourprice",
            "#priceblock_dealprice"
        };

        for (String selector : selectors) {
            Element priceElement = doc.selectFirst(selector);
            if (priceElement != null) {
                String priceText = priceElement.text().replaceAll("[^\\d.,]", "");
                if (!priceText.isEmpty()) {
                    try {
                        return Double.parseDouble(priceText.replace(",", ""));
                    } catch (NumberFormatException e) {
                        logger.warn("Failed to parse price from text: {}", priceText);
                    }
                }
            }
        }
        throw new ScraperException("Could not find or parse product price");
    }
}
