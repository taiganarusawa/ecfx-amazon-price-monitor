package com.taiga.pricemonitor.db;

public class PriceRecord {
    private final String productName;
    private final double price;
    private final String checkedAt;

    public PriceRecord(String productName, double price, String checkedAt) {
        this.productName = productName;
        this.price = price;
        this.checkedAt = checkedAt;
    }

    public String getProductName() { 
        return productName; 
    }

    public double getPrice() { 
        return price; 
    }
    
    public String getCheckedAt() { 
        return checkedAt; 
    }
}