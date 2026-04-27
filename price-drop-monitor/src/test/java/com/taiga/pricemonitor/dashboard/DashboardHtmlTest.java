package com.taiga.pricemonitor.dashboard;

import com.taiga.pricemonitor.config.ProductConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardHtmlTest {

    @Test
    public void rendersHtmlForProducts() {
        ProductConfig product = new ProductConfig();
        product.setName("Test Product");
        product.setUrl("https://amazon.com/dp/TEST123");

        String html = DashboardHtml.render(List.of(product));

        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("Test Product"));
        assertTrue(html.contains("Price Drop Monitor"));
    }

    @Test
    public void escapesHtmlSpecialCharacters() {
        ProductConfig product = new ProductConfig();
        product.setName("Test & <script>alert('xss')</script>");
        product.setUrl("https://amazon.com/dp/TEST");

        String html = DashboardHtml.render(List.of(product));

        assertFalse(html.contains("<script>alert"));
        assertTrue(html.contains("&lt;script&gt;"));
    }
}