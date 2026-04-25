package com.taiga.pricemonitor.comparison;

public class PriceDropResult {
    private final boolean isDrop;
    private final double previousPrice;
    private final double currentPrice;
    private final double dropPercent;

    public PriceDropResult(boolean isDrop, double previousPrice, double currentPrice, double dropPercent) {
        this.isDrop = isDrop;
        this.previousPrice = previousPrice;
        this.currentPrice = currentPrice;
        this.dropPercent = dropPercent;
    }

    public boolean isDrop() {
        return isDrop;
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getDropPercent() {
        return dropPercent;
    }
}
