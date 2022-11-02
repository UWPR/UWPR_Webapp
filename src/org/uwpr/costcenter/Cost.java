package org.uwpr.costcenter;

import java.math.BigDecimal;

public final class Cost
{
    public final BigDecimal instrumentCost;
    public final BigDecimal setupCost;

    public Cost(BigDecimal instrumentCost, BigDecimal setupCost) {
        this.instrumentCost = instrumentCost;
        this.setupCost = setupCost;
    }

    public BigDecimal getTotal()
    {
        return instrumentCost.add(setupCost);
    }

    public BigDecimal getInstrumentCost() {
        return instrumentCost;
    }

    public BigDecimal getSetupCost() {
        return setupCost;
    }
}
