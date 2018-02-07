/**
 * InstrumentRate.java
 * @author Vagisha Sharma
 * Apr 29, 2011
 */
package org.uwpr.costcenter;

import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.www.util.TimeUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 */
public class InstrumentRate {

	private int id;
	private MsInstrument instrument;
	private RateType rateType;
	private TimeBlock timeBlock;
	private BigDecimal rate;
	private Date createDate;
	private boolean isCurrent = false;
	
	public static BigDecimal SIGNUP_PERC = new BigDecimal("0.10");
	public static BigDecimal INSTRUMENT_PERC = new BigDecimal("0.90");
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public MsInstrument getInstrument() {
		return instrument;
	}
	public void setInstrument(MsInstrument instrument) {
		this.instrument = instrument;
	}
	public RateType getRateType() {
		return rateType;
	}
	public void setRateType(RateType rateType) {
		this.rateType = rateType;
	}
	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public TimeBlock getTimeBlock() {
		return timeBlock;
	}
	public void setTimeBlock(TimeBlock timeBlock) {
		this.timeBlock = timeBlock;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getCreateDateString() {
		return TimeUtils.shortDate.format(createDate);
	}
	public boolean isCurrent() {
		return isCurrent;
	}
	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public boolean isHourly()
	{
		return timeBlock.getName().equals(TimeBlock.HOURLY);
	}
}
