package org.uwpr.scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uwpr.costcenter.TimeBlock;
import org.uwpr.www.util.TimeUtils;

/**
 * TimeRangeSplitter.java
 * @author Vagisha Sharma
 * Jul 19, 2011
 * 
 */
public class TimeRangeSplitter {

	private static final TimeRangeSplitter instance = new TimeRangeSplitter();
	
	private static final Logger log = LogManager.getLogger(TimeRangeSplitter.class);
	
	private Calendar calendar = Calendar.getInstance();
	
	public static TimeRangeSplitter getInstance() {
		return instance;
	}
	
	public List<TimeBlock> split(Date startDate, Date endDate, List<TimeBlock> blocks) throws SchedulerException {
		
		if(blocks == null || blocks.size() == 0) {
			throw new SchedulerException("Cannot split date range into time blocks. No time blocks were given");
		}
		// sort the blocks by length (descending)
		Collections.sort(blocks, Collections.reverseOrder(new TimeBlockComparatorByLength()));
		
		List<TimeBlock> rangeBlocks = new ArrayList<TimeBlock>();
		
		
		Date newStartDate = startDate;
		
		while(newStartDate.before(endDate)) {
			
			double hoursInCurrentRange = getHoursInCurrentRange(newStartDate, endDate);
			
			for(TimeBlock block: blocks) {
				
				// If this block has start and end times we can use this block only 
				// if the current start time matches the start time of the block
				if(!matchesBlockStarTime(newStartDate, block)) {
					continue;
				}
				
				if(block.getNumHours() <= hoursInCurrentRange) {
					rangeBlocks.add(block);
					
					calendar.setTime(newStartDate);
					calendar.add(Calendar.HOUR_OF_DAY, block.getNumHours());
					newStartDate = calendar.getTime();
					
					break;
				}
			}
		}
		
		return rangeBlocks;
	}

	public List<TimeBlockRange> splitOnMonthEnd(Date startDate, Date endDate) {

		List<TimeBlockRange> rangeBlocks = new ArrayList<TimeBlockRange>();

		Calendar start = Calendar.getInstance();
		start.setTime(startDate);

		Calendar end = Calendar.getInstance();
		end.setTime(endDate);

		if(endDate.before(startDate))
		{
			return Collections.emptyList();
		}

		while(start.getTime().before(end.getTime())) {

			if(start.get(Calendar.YEAR) == end.get(Calendar.YEAR) && start.get(Calendar.MONTH) == end.get(Calendar.MONTH))
			{
				rangeBlocks.add(new TimeBlockRange(start.getTime(), end.getTime()));
				break;
			}

			Date s = start.getTime();
			int lastDayInMonth = start.getActualMaximum(Calendar.DAY_OF_MONTH);
			// start.add(Calendar.DAY_OF_MONTH, lastDayInMonth - start.get(Calendar.DAY_OF_MONTH));

			start.set(Calendar.DAY_OF_MONTH, lastDayInMonth);
			start.set(Calendar.MILLISECOND, 0);
			start.set(Calendar.SECOND, 0);
			start.set(Calendar.MINUTE, 0);
			start.set(Calendar.HOUR_OF_DAY, 0); // 12:00 am
			start.add(Calendar.MILLISECOND, TimeUtils.MILLIS_IN_DAY);
			Date e = start.getTime();

			rangeBlocks.add(new TimeBlockRange(s, e));
		}

		return rangeBlocks;
	}

	private boolean matchesBlockStarTime(Date startDate, TimeBlock block) {
		
		// If this block has no start and end time we return true
		if(block.getHasNoStartEndTime() || block.getNumHours() == 24)
			return true;
		
		calendar.setTime(startDate);
		int startHour = calendar.get(Calendar.HOUR_OF_DAY);
		int startMin = calendar.get(Calendar.MINUTE);
		
		calendar.setTime(block.getStartTime());
		int blockStartHour = calendar.get(Calendar.HOUR_OF_DAY);
		int blockStartMin = calendar.get(Calendar.MINUTE);
		
		return (startHour == blockStartHour) && (startMin == blockStartMin);
	}
	
	
	private double getHoursInCurrentRange(Date start, Date end) {
		
		long differenceInMillis = end.getTime() - start.getTime();
		return (double) differenceInMillis / ((double)(1000 * 60 * 60));
	}

	public static class TimeBlockRange
	{
		public final Date startDate;
		public final Date endDate;

		public TimeBlockRange(Date startDate, Date endDate) {
			this.startDate = startDate;
			this.endDate = endDate;
		}
	}
}
