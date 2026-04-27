package com.taiga.pricemonitor.comparison;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceComparator {
    private static final Logger logger = LoggerFactory.getLogger(PriceComparator.class);

    private final double thresholdPercent;

    public PriceComparator(double thresholdPercent) {
        this.thresholdPercent = thresholdPercent;
    }

    // Compares the current price against the previous price
    // Returns a result indicating whether the drop crossed the threshold
    public PriceDropResult compare(Double previousPrice, double currentPrice) {
        if (previousPrice == null) {
            logger.info("No previous price available, treating as no drop");
            return new PriceDropResult(false, 0.0, currentPrice, 0.0);
        }

        if (currentPrice >= previousPrice) {
            return new PriceDropResult(false, previousPrice, currentPrice, 0.0);
        }

        double dropPercent = ((previousPrice - currentPrice) / previousPrice) * 100;
        boolean meetsThreshold = dropPercent >= thresholdPercent;

        if (meetsThreshold) {
            logger.info("Price drop detected: previous={} current={} dropPercent={:.2f}%",
                    previousPrice, currentPrice, dropPercent);
        }

        return new PriceDropResult(meetsThreshold, previousPrice, currentPrice, dropPercent);
    }
}
