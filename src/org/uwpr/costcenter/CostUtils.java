package org.uwpr.costcenter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CostUtils
{
    public static BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public static BigDecimal calcCost(BigDecimal cost, BigDecimal percent)
    {
        return cost.multiply(percent).divide(ONE_HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }
}
