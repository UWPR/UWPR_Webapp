package org.uwpr.costcenter;

import java.math.BigDecimal;

import junit.framework.TestCase;

/**
 * calcCost gives one payment method's share of a block's cost.
 * Percents are scale 2, as instrumentUsagePayment.percentPayment stores them.
 */
public class CostUtilsTest extends TestCase {

    private static final BigDecimal BLOCK_COST = new BigDecimal("1756.00");

    public final void testOnePercent() {
        assertEquals("1 percent of 1756.00 should be 17.56",
                new BigDecimal("17.56"), CostUtils.calcCost(BLOCK_COST, new BigDecimal("1.00")));
    }

    public final void testHalfPercent() {
        assertEquals("0.5 percent of 1756.00 should be 8.78",
                new BigDecimal("8.78"), CostUtils.calcCost(BLOCK_COST, new BigDecimal("0.50")));
    }

    public final void testNinetyNinePercent() {
        assertEquals("99 percent of 1756.00 should be 1738.44",
                new BigDecimal("1738.44"), CostUtils.calcCost(BLOCK_COST, new BigDecimal("99.00")));
    }

    public final void testOneHundredPercent() {
        assertEquals("100 percent of 1756.00 should be 1756.00",
                new BigDecimal("1756.00"), CostUtils.calcCost(BLOCK_COST, new BigDecimal("100.00")));
    }

    public final void testZeroPercent() {
        assertEquals("0 percent of 1756.00 should be 0.00",
                new BigDecimal("0.00"), CostUtils.calcCost(BLOCK_COST, new BigDecimal("0.00")));
    }

    public final void testRoundsHalfUp() {
        assertEquals("33 percent of 46.50 is 15.345 and should round to 15.35",
                new BigDecimal("15.35"), CostUtils.calcCost(new BigDecimal("46.50"), new BigDecimal("33.00")));
    }
}
