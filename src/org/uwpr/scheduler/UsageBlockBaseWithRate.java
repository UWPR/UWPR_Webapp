/**
 * 
 */
package org.uwpr.scheduler;


import org.uwpr.costcenter.InstrumentRate;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.UsageBlockBase;

import java.math.BigDecimal;

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
		if(isDeleted())
		{
			return BigDecimal.ZERO; // 10.28.2022 - Sign-up cost no longer charged for deleted blocks
		}

		BigDecimal cost = rate.getRate();
		if(rate.isHourly())
		{
			cost = cost.multiply(new BigDecimal(getHours()));
		}
		return cost;
	}

	public BigDecimal getInstrumentCost()
	{
		return getCost();
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

	@Override
	public int getHours()
	{
		// If this instrument is an add-on (e.g. HPLC) then we do not want the hours
		// the instrument was used to count towards the total quota available to the user.
		// HPLC will always be used with a mass spec instrument. Only the total mass spec
		// instrument hours should count towards the quota.
		MsInstrument instrument = rate!= null ? rate.getInstrument() : null;
		return instrument != null && instrument.isMassSpec() ? super.getHours() : 0;
	}
}
