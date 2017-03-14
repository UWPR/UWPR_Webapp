package org.uwpr.instrumentlog;

import org.uwpr.costcenter.InstrumentRate;
import org.uwpr.www.util.TimeUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by vsharma on 2/2/2017.
 */
public class InstrumentSignupWithRate extends InstrumentSignupGeneric<SignupBlockWithRate>
{
    public BigDecimal getCost()
    {
        BigDecimal cost = BigDecimal.ZERO;
        List<SignupBlockWithRate> blocks = getBlocks();
        for(SignupBlockWithRate block: blocks)
        {
            cost.add(block.getCost());
        }
        return cost.multiply(InstrumentRate.SIGNUP_PERC);
    }
}
