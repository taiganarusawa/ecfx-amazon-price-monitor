package com.taiga.pricemonitor.dashboard;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DashboardHtmlTest {

    @Test
    public void loadsHtmlFromClasspath() {
        String html = DashboardHtml.render();

        assertNotNull(html);
        assertFalse(html.isEmpty());
    }

    @Test
    public void containsExpectedStructure() {
        String html = DashboardHtml.render();

        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("Price Drop Monitor"));
        assertTrue(html.contains("chart.js"));
        assertTrue(html.contains("/api/history"));
    }
}