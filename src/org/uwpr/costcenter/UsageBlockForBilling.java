/**
 * 
 */
package org.uwpr.costcenter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.scheduler.UsageBlockBaseWithRate;
import org.yeastrc.project.Project;
import org.yeastrc.project.Researcher;
import org.yeastrc.project.payment.PaymentMethod;

/**
 * UsageBlockForBilling.java
 * @author Vagisha Sharma
 * Sep 3, 2011
 * 
 */
public class UsageBlockForBilling {

	private List<UsageBlockBaseWithRate> blocks = null;
	private Project project;
	private Researcher user; // This is the person that signed up for instrument time
	private MsInstrument instrument;
	private PaymentMethod paymentMethod; // payment method and percent will be the same for all the blocks.
	private BigDecimal percent;
	private int totalHours = -1;
	private BigDecimal signupCost = null;
	private BigDecimal instrumentCost = null;
	private BigDecimal totalCost = null;
	
	public UsageBlockForBilling() {
		blocks = new ArrayList<UsageBlockBaseWithRate>();
	}
	
	public void add(UsageBlockBaseWithRate block) {
		this.blocks.add(block);
	}
	
	public List<UsageBlockBaseWithRate> getBlocks() {
		return blocks;
	}

	public int getFirstUsageBlockId() {
		
		if(blocks.size() == 0)
			return -1;
		return blocks.get(0).getID();
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Researcher getUser() {
		return user;
	}

	public void setUser(Researcher user) {
		this.user = user;
	}

	public MsInstrument getInstrument() {
		return instrument;
	}

	public void setInstrument(MsInstrument instrument) {
		this.instrument = instrument;
	}

	public int getTotalHours()
	{
		if(totalHours == -1)
		{
			totalHours = 0;
			for(UsageBlockBaseWithRate blk: blocks)
			{
				this.totalHours += blk.getRate().getTimeBlock().getNumHours();
			}
		}
		return totalHours;
	}

	public BigDecimal getTotalCost()
	{
		if(totalCost == null)
		{
			totalCost = BigDecimal.ZERO;
			for(UsageBlockBaseWithRate blk: blocks)
			{
				this.totalCost = totalCost.add(blk.getTotalCost());
			}
		}
		return totalCost;
	}

	public BigDecimal getSignupCost()
	{
		if(signupCost == null)
		{
			signupCost = BigDecimal.ZERO;
			for(UsageBlockBaseWithRate blk: blocks)
			{
				this.signupCost = signupCost.add(blk.getSignupCost());
			}
		}
		return signupCost;
	}

	public BigDecimal getInstrumentCost()
	{
		if(instrumentCost == null)
		{
			instrumentCost = BigDecimal.ZERO;
			for(UsageBlockBaseWithRate blk: blocks)
			{
				if(!blk.isDeleted())
				{
					instrumentCost = instrumentCost.add(blk.getInstrumentCost());
				}
			}
		}
		return instrumentCost;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public BigDecimal getBillingPercent() {
		return percent;
	}

	public void setBillingPercent(BigDecimal percent) {
		this.percent = percent;
	}
	
	public Date getStartDate() {
		if(blocks.size() == 0)
			return null;
		return blocks.get(0).getStartDate();
	}
	
	public Date getEndDate() {
		if(blocks.size() == 0)
			return null;
		return blocks.get(blocks.size() - 1).getEndDate();
	}
	
	public String getStartDateFormated() {
		if(blocks.size() == 0)
			return null;
		return blocks.get(0).getStartDateFormated();
	}
	
	public String getEndDateFormated() {
		if(blocks.size() == 0)
			return null;
		return blocks.get(blocks.size() - 1).getEndDateFormated();
	}
}
