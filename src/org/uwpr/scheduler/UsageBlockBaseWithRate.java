/**
 * 
 */
package org.uwpr.scheduler;

import org.uwpr.costcenter.CostUtils;
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
			return BigDecimal.ZERO; // 10.28.2022 - Sign-up cost no longer charged for deleted blocks
		}
		return cost;
	}

	public BigDecimal getSignupCost()
	{
		if(isDeleted())
		{
			return BigDecimal.ZERO; // 10.28.2022 - Sign-up cost no longer charged for deleted blocks
		}
		BigDecimal cost = getCost();
		return calcSignupCost(cost);
	}

	private static BigDecimal calcSignupCost(BigDecimal cost)
	{
		return CostUtils.calcCost(cost, InstrumentRate.SIGNUP_PERC);
	}

	public BigDecimal getInstrumentCost()
	{
		if(this.isDeleted())
		{
			return BigDecimal.ZERO;
		}
		BigDecimal cost = getCost();
		return CostUtils.calcCost(cost, InstrumentRate.INSTRUMENT_PERC);
	}

	public BigDecimal getSetupCost()
	{
		if(isSetupBlock())
		{
			// If the instrument fee is zero, do not add a setup fee. (This if for the "Pressure Cell & Laser Puller".
			return rate.getRate().doubleValue() == 0.0 ? BigDecimal.ZERO : rate.getRateType().getSetupFee();
		}
		else
		{
			return BigDecimal.ZERO;
		}
	}
}
