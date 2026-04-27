package com.taiga.pricemonitor.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class AmazonScraperTest {

    @Test
    public void parsesStandardPriceFormat() throws Exception {
        String html = """
            <html><body>
                <span id="productTitle">Test Product</span>
                <span class="a-price"><span class="a-offscreen">$29.99</span></span>
            </body></html>
            """;
        Document doc = Jsoup.parse(html);
        AmazonScraper scraper = new AmazonScraper();

        // Use reflection since extractPrice is private
        Method method = AmazonScraper.class.getDeclaredMethod("extractPrice", Document.class);
        method.setAccessible(true);
        double price = (double) method.invoke(scraper, doc);

        assertEquals(29.99, price, 0.001);
    }

    @Test
    public void parsesPriceWithCommaThousandsSeparator() throws Exception {
        String html = """
            <html><body>
                <span class="a-price"><span class="a-offscreen">$1,299.99</span></span>
            </body></html>
            """;
        Document doc = Jsoup.parse(html);
        AmazonScraper scraper = new AmazonScraper();

        Method method = AmazonScraper.class.getDeclaredMethod("extractPrice", Document.class);
        method.setAccessible(true);
        double price = (double) method.invoke(scraper, doc);

        assertEquals(1299.99, price, 0.001);
    }

    @Test
    public void throwsWhenNoPriceFound() throws Exception {
        String html = "<html><body><p>No price here</p></body></html>";
        Document doc = Jsoup.parse(html);
        AmazonScraper scraper = new AmazonScraper();

        Method method = AmazonScraper.class.getDeclaredMethod("extractPrice", Document.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () -> method.invoke(scraper, doc));
        // The actual cause is a ScraperException wrapped by reflection
        assertTrue(exception.getCause() instanceof ScraperException);
    }
}