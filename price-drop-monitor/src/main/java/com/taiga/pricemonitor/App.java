package com.taiga.pricemonitor;

import com.taiga.pricemonitor.config.AppConfig;
import com.taiga.pricemonitor.config.ConfigLoader;
import com.taiga.pricemonitor.db.DatabaseService;

public class App {
    public static void main(String[] args) {
        AppConfig config = ConfigLoader.load("config.yaml");
        DatabaseService db = new DatabaseService("prices.db");

        db.savePrice("https://www.amazon.com/dp/B09XS7JWHH", "Sony Headphones", 279.99);
        Double lastPrice = db.getLastPrice("https://www.amazon.com/dp/B09XS7JWHH");
        System.out.println("Last price retrieved: $" + lastPrice);
    }
}