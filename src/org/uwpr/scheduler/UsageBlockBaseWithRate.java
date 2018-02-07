/**
 * 
 */
package org.uwpr.scheduler;

import org.uwpr.costcenter.InstrumentRate;
import org.uwpr.instrumentlog.UsageBlockBase;
import org.uwpr.instrumentlog.UsageBlockBaseFilter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * UsageBlockBaseWithRate.java
 * @author Vagisha Sharma
 * Jul 19, 2011
 * 
 */
public class UsageBlockBaseWithRate extends UsageBlockBase {

	private InstrumentRate rate;

	public InstrumentRate getRate() {
		return rate;
	}

	public void setRate(InstrumentRate rate) {
		this.rate = rate;
	}

	public BigDecimal getTotalCost()
	{
		return getCost().add(getSetupCost()); // Add any setup cost
	}

	public BigDecimal getCost()
	{
		BigDecimal cost = rate.getRate();
		if(rate.isHourly())
		{
			cost = cost.multiply(new BigDecimal(getHours()));
		}
		if(isDeleted())
		{
			return calcSignupCost(cost);
		}
		return cost;
	}

	public BigDecimal getSignupCost()
	{
		BigDecimal cost = getCost();
		if(isDeleted())
		{
			return cost; // Only sign-up cost in this case
		}
		return calcSignupCost(cost);
	}

	private static BigDecimal calcSignupCost(BigDecimal cost)
	{
		return cost.multiply(InstrumentRate.SIGNUP_PERC).setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal getInstrumentCost()
	{
		if(this.isDeleted())
		{
			return BigDecimal.ZERO;
		}
		BigDecimal cost = getCost();
		return cost.multiply(InstrumentRate.INSTRUMENT_PERC).setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal getSetupCost()
	{
		return isSetupBlock() ? rate.getRateType().getSetupFee() : BigDecimal.ZERO;
	}
}
