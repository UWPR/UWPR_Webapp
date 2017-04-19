/**
 * 
 */
package org.uwpr.scheduler;

import org.uwpr.costcenter.InstrumentRate;
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
		return getSignupCost().add(getInstrumentCost());
	}

	public BigDecimal getSignupCost()
	{
		return rate.getSignupFee();
	}

	public BigDecimal getInstrumentCost()
	{
		return isDeleted() ? BigDecimal.ZERO : rate.getInstrumentFee();
	}
}
