package com.taiga.pricemonitor.comparison;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PriceComparatorTest {

    @Test
    public void detectsPriceDropAboveThreshold() {
        PriceComparator comparator = new PriceComparator(5.0);
        PriceDropResult result = comparator.compare(100.0, 90.0);

        assertTrue(result.isDrop());
        assertEquals(10.0, result.getDropPercent(), 0.01);
    }

    @Test
    public void doesNotDetectDropBelowThreshold() {
        PriceComparator comparator = new PriceComparator(5.0);
        PriceDropResult result = comparator.compare(100.0, 98.0);

        assertFalse(result.isDrop());
    }

    @Test
    public void doesNotDetectDropWhenPriceIncreases() {
        PriceComparator comparator = new PriceComparator(5.0);
        PriceDropResult result = comparator.compare(100.0, 110.0);

        assertFalse(result.isDrop());
    }

    @Test
    public void handlesNullPreviousPriceGracefully() {
        PriceComparator comparator = new PriceComparator(5.0);
        PriceDropResult result = comparator.compare(null, 100.0);

        assertFalse(result.isDrop());
    }

    @Test
    public void detectsExactThresholdAsDrop() {
        PriceComparator comparator = new PriceComparator(10.0);
        PriceDropResult result = comparator.compare(100.0, 90.0);

        assertTrue(result.isDrop());
    }
}