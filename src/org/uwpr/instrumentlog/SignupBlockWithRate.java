package org.uwpr.instrumentlog;

import org.uwpr.costcenter.InstrumentRate;

import java.math.BigDecimal;

/**
 * Created by vsharma on 2/10/2017.
 */
public class SignupBlockWithRate extends SignupBlock
{
    private InstrumentRate rate;

    public InstrumentRate getRate() {
        return rate;
    }

    public void setRate(InstrumentRate rate)
    {
        this.rate = rate;
    }

    public BigDecimal getCost()
    {
        return rate != null ? rate.getRate() : BigDecimal.ZERO;
    }
}
