package com.taiga.pricemonitor.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DashboardHtml {

    private static final String HTML;

    static {
        try (InputStream in = DashboardHtml.class.getResourceAsStream("/dashboard.html")) {
            if (in == null) {
                throw new IllegalStateException("dashboard.html not found on classpath");
            }
            HTML = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load dashboard.html", e);
        }
    }

    public static String render() {
        return HTML;
    }
}