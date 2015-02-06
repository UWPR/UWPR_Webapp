package org.uwpr.instrumentlog;

import java.util.Date;
import java.util.List;

public abstract class UsageRange <T extends UsageBlockBase>{

	private Date startTime;
	private Date endTime;
	private float numHoursUsed = -1;
	private float sumHoursUsed = -1;
	private float percentUsed = -1.0f;

	protected abstract List <T> getAllUsageBlocks();
	
	
	public UsageRange(Date startDate, Date endDate) {
		this.startTime = startDate;
		this.endTime = endDate;
	}
	
	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * Returns the number of hours covered by the UsageBlocks in this UsageRange. 
	 * Overlapping blocks are merged.
	 * @return
	 */
	public float getNumHoursUsed() {
		if (numHoursUsed != -1)
			return numHoursUsed;
		
		// get all the usage blocks
		List <T> allBlks = getAllUsageBlocks();
		return numHoursUsed(allBlks);
	}
	
   /**
     * Returns the number of hours covered by the UsageBlocks in this UsageRange. 
     * Overlapping blocks are merged.
     * @return
     */
	private float numHoursUsed(List<T> allBlks) {

		// merge overlapping usage blocks
		List<T> mergedBlks = UsageBlockBase.mergeUsageBlocks(allBlks);
		
		// add up the hours in all the usage blocks.
		float hourCount = 0;
		for (T blk: mergedBlks) 
			hourCount += DateUtils.getNumHoursUsed(blk.getStartDate(), blk.getEndDate());
		numHoursUsed = hourCount;
		
		return numHoursUsed;
	}
	

	public float getHoursInRange() {
        return DateUtils.getNumUsableHours(startTime, endTime);
    }
	
	/**
	 * Returns the number of hours covered by the UsageBlocks in this UsageRange. 
	 * Overlaps are not removed.
	 * @return
	 */
	public float getSumHoursUsed() {
		if (sumHoursUsed != -1)
			return sumHoursUsed;
		sumHoursUsed = 0;
		List <T> allBlks = getAllUsageBlocks();
		for (T blk: allBlks)
			sumHoursUsed += DateUtils.getNumHoursUsed(blk.getStartDate(), blk.getEndDate());
		return sumHoursUsed;
	}
	
	public Float getPercentUsed() {
		if (percentUsed != -1)
			return percentUsed;
		float totalUsableHours = DateUtils.getNumUsableHours(startTime, endTime);
		float usedHours = getNumHoursUsed();
		percentUsed = (Math.round((usedHours*1000)/(float)totalUsableHours))/10.0f;
		return percentUsed;
	}
	
}