package org.uwpr.costcenter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CostUtils
{
    public static BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public static BigDecimal calcCost(BigDecimal cost, BigDecimal percent)
    {
        if(percent.doubleValue() == 100.0)
        {
            return cost.setScale(2, RoundingMode.HALF_UP);
        }
        cost = cost.multiply(percent);
        if(percent.doubleValue() > 1.0)
        {
            cost = cost.divide(ONE_HUNDRED);
        }

        return cost.setScale(2, RoundingMode.HALF_UP);
    }
}
