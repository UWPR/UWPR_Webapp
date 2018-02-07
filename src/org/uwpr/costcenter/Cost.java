package org.uwpr.costcenter;

import java.math.BigDecimal;

public final class Cost
{
    public final BigDecimal instrumentCost;
    public final BigDecimal signupCost;
    public final BigDecimal setupCost;

    public Cost(BigDecimal instrumentCost, BigDecimal signupCost, BigDecimal setupCost) {
        this.instrumentCost = instrumentCost;
        this.signupCost = signupCost;
        this.setupCost = setupCost;
    }

    public BigDecimal getTotal()
    {
        return instrumentCost.add(signupCost).add(setupCost);
    }

    public BigDecimal getInstrumentCost() {
        return instrumentCost;
    }

    public BigDecimal getSignupCost() {
        return signupCost;
    }

    public BigDecimal getSetupCost() {
        return setupCost;
    }
}
