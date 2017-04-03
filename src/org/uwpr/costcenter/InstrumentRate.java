/**
 * InstrumentRate.java
 * @author Vagisha Sharma
 * Apr 29, 2011
 */
package org.uwpr.costcenter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.UsageBlockBaseFilter;

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

	private static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	
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

	public BigDecimal getSignupFee()
	{
		return getSignupCost(this.rate);
	}

	public BigDecimal getInstrumentFee()
	{
		return getInstrumentCost(this.rate);
	}

	public BigDecimal getCost(UsageBlockBaseFilter.BlockType blockType)
	{
		if(blockType == UsageBlockBaseFilter.BlockType.ALL)
		{
			return this.rate;
		}
		else if(blockType == UsageBlockBaseFilter.BlockType.SIGNUP_ONLY)
		{
			return getSignupFee();
		}
		else if(blockType == UsageBlockBaseFilter.BlockType.INSTRUMENT_USAGE)
		{
			return getInstrumentFee();
		}
		else
			return BigDecimal.ZERO;
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
		return dateFormat.format(createDate);
	}
	public boolean isCurrent() {
		return isCurrent;
	}
	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public static BigDecimal getSignupCost(BigDecimal rate)
	{
		return rate.multiply(SIGNUP_PERC).setScale(2, RoundingMode.HALF_UP);
	}

	public static BigDecimal getInstrumentCost(BigDecimal rate)
	{
		return rate.multiply(INSTRUMENT_PERC).setScale(2, RoundingMode.HALF_UP);
	}
}
