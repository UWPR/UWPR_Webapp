package org.uwpr.instrumentlog;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.uwpr.chart.google.DataPoint;
import org.uwpr.chart.google.DataSeries;
import org.uwpr.chart.google.DataSet;

public class UsageStatsCalculator {

	private static final String piStatsColor = "4B0082";
	private static final String projectStatsColor = "800080"; // 483D8B
	private static final String maintenanceColor =  "CCCCCC"; // "FFD700";
	// private static final String billedProjectColor = "808080";
	
	private static final int MAINTENANCE_PROJ = 42;
	
    
    private UsageStatsCalculator() {}
	
	//------------------------------------------------------------------------------------------
	// Usage by PI
	//------------------------------------------------------------------------------------------
	public static DataSet calculatePIStats(List <UsageBlock> usageBlks, 
			java.util.Date startDate, java.util.Date endDate, int instrumentCount) {
		
		// calculate the total time used
		// add up the time
		float totalHoursUsed = 0;
		for (UsageBlock blk: usageBlks) {
		    totalHoursUsed += DateUtils.getNumHoursUsed(blk.getStartDate(), blk.getEndDate());
		}
		
		// sort usage by PI
        Collections.sort(usageBlks, new Comparator<UsageBlock> () {
            @Override
            public int compare(UsageBlock o1, UsageBlock o2) {
                return o1.getProjectPI().compareTo(o2.getProjectPI());
            }});
        
		// group usage by PI
		List <MyUsageRange> usageRangeList = groupUsageByPI(usageBlks, startDate, endDate);
		
		// create a dataset
		DataSet piStats = new DataSet("Usage(%) by PI");
		DataSeries series = new DataSeries("PI");
		series.setSeriesColor(piStatsColor);
		piStats.addSeries(series);
		for (MyUsageRange range: usageRangeList) {
			float percentUsed = getPercent(range.getSumHoursUsed(), totalHoursUsed);
//					DateUtils.getNumUsableHours(startDate, endDate)*instrumentCount);
			series.addDataPoint(((MyUsageRange)range).getName()+"("+percentUsed+"%)", percentUsed);
		}
		piStats.sort();
		return piStats;
	}
	
	private static List<MyUsageRange> groupUsageByPI(List <UsageBlock> usageBlks, java.util.Date startDate, 
	        java.util.Date endDate) {

		String currPI = null;
		List <MyUsageRange> usageRangeList = new ArrayList<MyUsageRange>();
		MyUsageRange usageRange = null;

		for (UsageBlock blk: usageBlks) {
			String thisPI = blk.getProjectPI();

			if (currPI == null  || !currPI.equals(thisPI)) {
				currPI = thisPI;
				usageRange = new MyUsageRange(new Date(startDate.getTime()),
				        new Date(endDate.getTime()),
				        blk.getProjectPI());
				usageRangeList.add(usageRange);
			}
			usageRange.addUsageBlock(blk);
		}
		
		// NOTE: Commented out (02/25/09) -- THIS SHOULD NOT HAPPEN
//		// We only want the number of days a PI used a particular instruments.  In case the PI has overlapping usage on the 
//		// same instrument (e.g. two different projects run on the same days on the the instrument) we need to 
//		// merge the usage so that it is not counted twice
//		for (MyUsageRange piUsageRange: usageRangeList) {
//			// group by instrument and and merge overlapping blocks
//			List <MyUsageRange> rangeList = groupUsageByInstrument(piUsageRange.getAllUsageBlocks(), startDate, endDate);
//			// clear the usage blocks in piUsageRange
//			piUsageRange.clearBlocks();
//			for (MyUsageRange r: rangeList) {
//				List <UsageBlock> mergedBlocks = UsageRange.mergeUsageBlocks(r.getAllUsageBlocks());
//				// add merged blocks to the piUsageRange
//				for (UsageBlock blk: mergedBlocks) {
//					piUsageRange.addUsageBlock(blk);
//				}
//			}
//			
//		}
		return usageRangeList;
	}

	//------------------------------------------------------------------------------------------
    // Usage by Project
    //------------------------------------------------------------------------------------------
    public static DataSet calculateProjectStats(List <UsageBlock> usageBlks, java.util.Date startDate, 
            java.util.Date endDate) {
        // sort usage by Project
        Collections.sort(usageBlks, new Comparator<UsageBlock> () {
            @Override
            public int compare(UsageBlock o1, UsageBlock o2) {
                return Integer.valueOf(o1.getProjectID()).compareTo(o2.getProjectID());
            }});
        
        // group usage by Project
        List <MyUsageRange> usageRangeList = groupUsageByProject(usageBlks, startDate, endDate);
        
        // create a dataset
        DataSet projectStats = new DataSet("Hours / Project (Top 10)");
        DataSeries series = new DataSeries("Project ID");
        
        for (MyUsageRange range: usageRangeList) {
            series.addDataPoint(((MyUsageRange)range).getName(), range.getSumHoursUsed());
        }
        series.sortReverse();
        series.limitDataPoints(10); // keep the top 10 projects.
        series.setSeriesColor(projectStatsColor);
        
        // if the top 10 projects contain maintenence projects they should be highlighted
        boolean hasMaintenance = false;
        for(DataPoint pt: series.getDataPoints()) {
            if(pt.getLabel().equals(String.valueOf(MAINTENANCE_PROJ))) {
                hasMaintenance = true;
                break;
            }
        }
        if(hasMaintenance) {
            series.setUseDataPointColors(true);
            for(DataPoint pt: series.getDataPoints()) {
                if(pt.getLabel().equals(String.valueOf(MAINTENANCE_PROJ))) 
                    pt.setColor(maintenanceColor);
                else
                    pt.setColor(projectStatsColor);
            }
        }
        projectStats.addSeries(series); 
        
        return projectStats;
    }
    
    private static List<MyUsageRange> groupUsageByProject(List <UsageBlock> usageBlks, 
            java.util.Date startDate, java.util.Date endDate) {

        int currProjectId = -1;
        List <MyUsageRange> usageRangeList = new ArrayList<MyUsageRange>();
        MyUsageRange usageRange = null;

        for (UsageBlock blk: usageBlks) {
            int thisID = blk.getProjectID();

            if (currProjectId == -1 || currProjectId != thisID) {
                currProjectId = thisID;
                usageRange = new MyUsageRange(new Date(startDate.getTime()),
                        new Date(endDate.getTime()), ""+currProjectId);
                usageRangeList.add(usageRange);
            }
            usageRange.addUsageBlock(blk);
        }
        return usageRangeList;
    }
    
	//------------------------------------------------------------------------------------------
	// Usage by Instrument
	//------------------------------------------------------------------------------------------
	public static DataSet calculateInstrumentStats(List <UsageBlock> usageBlks, List <MsInstrument> instruments,
			java.util.Date startDate, java.util.Date endDate) {
		// number of hours over which we are collecting stats
//		int totalDays = DateUtils.getNumDays(startDate, endDate);
	    float totalUsableHours = DateUtils.getNumUsableHours(startDate, endDate);
		
		
		// group usage by instrument ID
		List <MyUsageRange> usageRangeList = groupUsageByInstrument(usageBlks, instruments, startDate, endDate, null);
		// group usage by instrumentID (only maintenance projects)
		Set<Integer> maintProjectIds = new HashSet<Integer>(1);
		maintProjectIds.add(MAINTENANCE_PROJ);
		List<MyUsageRange> maintUsageRangeList = groupUsageByInstrument(usageBlks, instruments, startDate, endDate, maintProjectIds);
		// TODO: group usage by instrumentID (only billed projects)
		//Set<Integer> billedProjectIds = getBilledProjectIds();
		//List<MyUsageRange> billedUsageRangeList = groupUsageByInstrument(usageBlks, instruments, startDate, endDate, billedProjectIds);
		
		
		// create a dataset
		DataSet instrumentStats = new DataSet("Usage(%) by Instrument");
		
		DataSeries maintSeries = new DataSeries("maintInstrument");
		maintSeries.setSeriesColor(maintenanceColor);
		//DataSeries billedProjSeries = new DataSeries("billedProject");
		//billedProjSeries.setSeriesColor(billedProjectColor);
		DataSeries series = new DataSeries("instrument");
		series.setUseDataPointColors(true);
		
		instrumentStats.addSeries(maintSeries);
		//instrumentStats.addSeries(billedProjSeries);
		instrumentStats.addSeries(series);

		Map<Integer, MsInstrument> instrumentMap = new HashMap<Integer, MsInstrument>();
		for(MsInstrument instrument: instruments)
		{
			instrumentMap.put(instrument.getID(), instrument);
		}
		for(int i = 0; i < usageRangeList.size(); i++) {
		    MyUsageRange r = usageRangeList.get(i);
		    MyUsageRange mr = maintUsageRangeList.get(i);
		    
		    if(r.getNumHoursUsed() == 0 && mr.getNumHoursUsed() == 0) {
		    	continue;
		    }
		    
		    float nomaint = r.getNumHoursUsed() - mr.getNumHoursUsed();
		    
		    DataPoint dp = new DataPoint(r.getName(), getPercent(nomaint, totalUsableHours));
		    if(r.getAllUsageBlocks().size() > 0) {
		        int instrumentId = r.getAllUsageBlocks().get(0).getInstrumentID();
				String color = instrumentMap.get(instrumentId).getColor();
		        dp.setColor(StringUtils.isBlank(color) ? InstrumentColors.getColor(instrumentId) : color);
		    }
		    series.addDataPoint(dp);
		    
		    maintSeries.addDataPoint(mr.getName(), getPercent(mr.getNumHoursUsed(), totalUsableHours));
		}

//		instrumentStats.sort();
		return instrumentStats;
	}
	
	private static List <MyUsageRange> groupUsageByInstrument(List <UsageBlock> usageBlks, 
	        List<MsInstrument> instruments, java.util.Date startDate, java.util.Date endDate, Set<Integer> projectIds) {

	    // sorts the instruments by id
	    Collections.sort(instruments, new Comparator<MsInstrument> () {
            @Override
            public int compare(MsInstrument o1, MsInstrument o2) {
                return Integer.valueOf(o1.getID()).compareTo(o2.getID());
            }});
	    
	    //sort usage blocks by instrumentID
        Collections.sort(usageBlks, new Comparator<UsageBlock> () {
            @Override
            public int compare(UsageBlock o1, UsageBlock o2) {
                return o1.getInstrumentID() > o2.getInstrumentID() ? 1 : (o1.getInstrumentID() == o2.getInstrumentID() ? 0 : -1);
            }});
        
        
        List <MyUsageRange> usageRangeList = new ArrayList<MyUsageRange>();
        int uid = 0;
        for(MsInstrument instrument: instruments) {
            int currID = instrument.getID();
            
            MyUsageRange usageRange = new MyUsageRange(
                    new Date(startDate.getTime()), 
                    new Date(endDate.getTime()),
                    instrument.getName()+"("+currID+")");
            usageRangeList.add(usageRange);
            for(int i = uid; i < usageBlks.size(); i++) {
                
                UsageBlock blk = usageBlks.get(i);
                if(blk.getInstrumentID() != currID) {
                    uid = i;
                    break;
                }
                else {
                    if(projectIds != null && projectIds.size() > 0 && !projectIds.contains(blk.getProjectID()))
                        continue;
                    usageRange.addUsageBlock(blk);
                }
            }
        }
        
		return usageRangeList;
	}
	
	//------------------------------------------------------------------------------------------
	// Usage by Month
	//------------------------------------------------------------------------------------------
	public static DataSet calculateMonthlyStats(List <UsageBlock> usageBlks, List<MsInstrument> instruments, Date startDate, Date endDate) {
		return calculateMonthlyStats(usageBlks, instruments, startDate, endDate, true);
	}
	
	public static DataSet calculateMonthlyStats(List <UsageBlock> usageBlks, List<MsInstrument> instruments,
	        Date startDate, Date endDate, boolean allTrendLine) {
		
		//sort usage blocks by instrumentID
		Collections.sort(usageBlks, new Comparator<UsageBlock> () {
			@Override
			public int compare(UsageBlock o1, UsageBlock o2) {
				return o1.getInstrumentID() > o2.getInstrumentID() ? 1 : (o1.getInstrumentID() == o2.getInstrumentID() ? 0 : -1);
			}});
		
		// group usage by instrument ID
		List <MyUsageRange> usageRangeList = groupUsageByInstrument(usageBlks, instruments, startDate, endDate, null);
		
		// split the given date range into month blocks
		List <MonthBlock> monthBlocks = makeMonthBlocks(startDate, endDate);
		
		// create dataset
		DataSet monthlyStats = new DataSet("Usage(%) by Month");
		
		if (allTrendLine) {
			// create overall series
			MyUsageRange allUsageRange = new MyUsageRange(new Date(startDate.getTime()),
                    new Date(endDate.getTime()), "All");
			for (UsageBlock blk: usageBlks)
				allUsageRange.addUsageBlock(blk);
			monthlyStats.addSeries(monthlyStatsForUsageRange(monthBlocks, allUsageRange));
		}

		Map<Integer, MsInstrument> instrumentMap = new HashMap<Integer, MsInstrument>();
		for(MsInstrument instrument: instruments)
		{
			instrumentMap.put(instrument.getID(), instrument);
		}

		// create a series for each instrument
		for (MyUsageRange usageRange: usageRangeList) {
		    if(usageRange.getAllUsageBlocks().size() == 0) continue;
		    DataSeries series = monthlyStatsForUsageRange(monthBlocks, usageRange);
		    int instrumentId = usageRange.getAllUsageBlocks().get(0).getInstrumentID();
			String color = instrumentMap.get(instrumentId).getColor();
			series.setSeriesColor(StringUtils.isBlank(color) ? InstrumentColors.getColor(instrumentId) : color);
			monthlyStats.addSeries(series);
		}
		
		return monthlyStats;
	}
	
	private static DataSeries monthlyStatsForUsageRange(List <MonthBlock> monthBlocks, MyUsageRange usageRange) {
		
		// merge the usage blocks
		List <UsageBlock> mergedBlks = UsageBlock.mergeUsageBlocks(usageRange.getAllUsageBlocks());
		
		// sort by start date
		Collections.sort(mergedBlks, new Comparator<UsageBlock>() {
			public int compare(UsageBlock o1, UsageBlock o2) {
				return o1.getStartDate().compareTo(o2.getStartDate());
			}});
		
		// reset the MonthBlocks
		for (MonthBlock mBlk: monthBlocks)
			mBlk.reset();
		
		// bin usage blocks into the MonthBlocks
		for (MonthBlock mBlk: monthBlocks) {
			for (UsageBlock blk: mergedBlks) {
				if (mBlk.end.compareTo(blk.getStartDate()) == -1)
					break;
				if (mBlk.begin.compareTo(blk.getEndDate()) < 1) {
					Date s = DateUtils.maxTimestamp(mBlk.begin, blk.getStartDate());
					Date e = DateUtils.minTimestamp(mBlk.end, blk.getEndDate());
					mBlk.addUsage(DateUtils.getNumHoursUsed(s, e));
				}
			}
		}
		
		// create the DataSeries
		DataSeries series = new DataSeries(usageRange.getName());
		for (MonthBlock mBlk: monthBlocks) {
			series.addDataPoint(mBlk.getName(), getPercent(mBlk.hoursUsed(), mBlk.totalUsableHours()));
		}
		return series;
	}
	
	static List <MonthBlock> makeMonthBlocks(Date startDate, Date endDate) {
		Calendar cal = GregorianCalendar.getInstance();
		Date currEnd = startDate;
		
		String[] months = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		
		List <MonthBlock> mBlks = new ArrayList<MonthBlock>();
		while (currEnd.compareTo(endDate) < 0) {
			// get the month
			cal.setTime(currEnd);
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Date begin = new Date(cal.getTimeInMillis());
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            
			Date end = new Date(cal.getTimeInMillis());
			MonthBlock mBlk = new MonthBlock(year, months[month], begin, end);
			mBlks.add(mBlk);
			
			// advance by one day to first of next month
			cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			currEnd = new Date(cal.getTimeInMillis());
		}
		return mBlks;
	}
	
	private static float getPercent(float actual, float total) {
		return Math.round((actual*1000.0f)/total)/10.0f;
	}
	
	//------------------------------------------------------------------------------------------
	// Class MonthBlock
	//------------------------------------------------------------------------------------------
	public static class MonthBlock {
		String month;
		int year;
		Date begin;
		Date end;
		
		float used = 0;
		
		public MonthBlock(int year, String month, Date begin, Date end) {
			this.year = year;
			this.month = month;
			this.begin = begin;
			this.end = end;
		}
		
		public String toString() {
			return month+" "+year+"; Begin: "+begin+"; End: "+end;
		}
		
		public void addUsage(float hours) {
			used+= hours;
		}
		
		public float hoursUsed() {
			return used;
		}
		
		public float totalUsableHours() {
			return DateUtils.getNumUsableHours(begin, end);
		}
		
		public String getName() {
			StringBuilder buf = new StringBuilder();
			buf.append(year);
			if (buf.length() > 2) {
				buf.delete(0, 2);
			}
			buf.insert(0, month+"'");
			return buf.toString();
		}
		
		public void reset() {
			used = 0;
		}
	}
	
	//------------------------------------------------------------------------------------------
	// Class MyUsageRange
	//------------------------------------------------------------------------------------------
	private static class MyUsageRange extends UsageRange {

		private List <UsageBlock> myBlocks;
		private String name;
		
		public MyUsageRange(Date startDate, Date endDate, String name) {
			super(startDate, endDate);
			myBlocks = new ArrayList<UsageBlock>();
			this.name = name;
		}
		
		@Override
		protected List<UsageBlock> getAllUsageBlocks() {
			return myBlocks;
		}
		
		public void clearBlocks() {
			myBlocks.clear();
		}
		
		public void addUsageBlock(UsageBlock blk) {
			myBlocks.add(blk);
		}
		
		public String getName() {
			return name;
		}
	}
}
