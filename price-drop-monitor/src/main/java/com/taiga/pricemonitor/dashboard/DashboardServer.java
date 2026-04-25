package com.taiga.pricemonitor.dashboard;

import com.google.gson.Gson;
import com.taiga.pricemonitor.config.AppConfig;
import com.taiga.pricemonitor.config.ProductConfig;
import com.taiga.pricemonitor.db.DatabaseService;
import com.taiga.pricemonitor.db.PriceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardServer {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServer.class);
    private static final int PORT = 4567;

    private final AppConfig config;
    private final DatabaseService db;
    private final Gson gson = new Gson();

    public DashboardServer(AppConfig config, DatabaseService db) {
        this.config = config;
        this.db = db;
    }

    public void start() {
        Spark.port(PORT);

        // Main dashboard page
        Spark.get("/", (req, res) -> {
            res.type("text/html");
            return DashboardHtml.render(config.getProducts());
        });

        // JSON endpoint that returns price history for all products
        Spark.get("/api/history", (req, res) -> {
            res.type("application/json");
            Map<String, Object> response = new HashMap<>();

            for (ProductConfig product : config.getProducts()) {
                List<PriceRecord> history = db.getPriceHistory(product.getUrl());
                Map<String, Object> productData = new HashMap<>();
                productData.put("name", product.getName());
                productData.put("history", history);
                response.put(product.getUrl(), productData);
            }

            return gson.toJson(response);
        });

        Spark.awaitInitialization();
        logger.info("Dashboard server running at http://localhost:{}", PORT);
    }

    public void stop() {
        Spark.stop();
        logger.info("Dashboard server stopped");
    }
}