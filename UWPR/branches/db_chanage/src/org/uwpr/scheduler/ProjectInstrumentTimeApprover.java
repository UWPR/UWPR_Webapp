/**
 * ProjectInstrumentTimeApprover.java
 * @author Vagisha Sharma
 * Jun 15, 2011
 */
package org.uwpr.scheduler;

import org.apache.log4j.Logger;
import org.uwpr.instrumentlog.UsageBlockBase;
import org.uwpr.instrumentlog.UsageBlockBaseDAO;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;

import java.sql.SQLException;
import java.util.*;

/**
 * 
 */
public class ProjectInstrumentTimeApprover {

	private static final ProjectInstrumentTimeApprover instance = new ProjectInstrumentTimeApprover();
	
	private static final Logger log = Logger.getLogger(ProjectInstrumentTimeApprover.class);
	
	public static int HOURS_QUOTA_BILLED_PROJECT = 24 * 20; // 480 hours
	public static int HOURS_QUOTA_FREE_PROJECT = 24 * 5;
	
	private ProjectInstrumentTimeApprover() {}
	
	public static ProjectInstrumentTimeApprover getInstance() {
		return instance;
	}

	public TimeRequest processTimeRequest(User user, Researcher instrumentOperator,
										  List<? extends UsageBlockBase> blocksBeingScheduled)
			                                 throws SQLException
	{
		return processTimeRequest(user, instrumentOperator, blocksBeingScheduled, Collections.<UsageBlockBase>emptyList());
	}

	public TimeRequest processTimeRequest(User user, Researcher instrumentOperator,
										  List<? extends UsageBlockBase> blocksBeingScheduled,
										  List<UsageBlockBase> oldBlocksToIgnore)
			throws SQLException
	{
		// Requests by admins should always be approved.
		if(Groups.getInstance().isAdministrator(user.getResearcher()))
		{
			return APPROVED;
		}

		int unusedHours = getUnusedInstrumentTimeForOperator(instrumentOperator, oldBlocksToIgnore);
		int totalRequested = getTotalHours(blocksBeingScheduled);

		log.info("Total hours requested for instrument operator (" + instrumentOperator.getID() + ", " + instrumentOperator.getFullName() + "): " + totalRequested);
		return new TimeRequest(unusedHours, totalRequested);
	}

	public static int getRemainingInstrumentTimeForOperator(Researcher researcher) throws SQLException
	{
		int timeUsed = getUnusedInstrumentTimeForOperator(researcher, Collections.<UsageBlockBase>emptyList());
		TimeRequest request = new TimeRequest(timeUsed, 0);
		return request.getTimeRemaining();
	}

	private static int getUnusedInstrumentTimeForOperator(Researcher operator, List<UsageBlockBase> blocksToIgnore) throws SQLException
	{
		if(Groups.getInstance().isAdministrator(operator))
		{
			return APPROVED.getTimeUsed();
		}
		Date now = new Date();
		List<UsageBlockBase> usageBlocks = UsageBlockBaseDAO.getUsageBlocksForInstrumentOperator(operator.getID(), now, null,
				true); // return blocks that are contained in the given date range.

		if(blocksToIgnore != null && blocksToIgnore.size() > 0)
		{
			Set<Integer> blockIdsToIgnore = new HashSet<Integer>();
			for(UsageBlockBase block: blocksToIgnore)
			{
				blockIdsToIgnore.add(block.getID());
			}

			Iterator<UsageBlockBase> iterator = usageBlocks.iterator();
			while(iterator.hasNext())
			{
				if(blockIdsToIgnore.contains(iterator.next().getID()))
				{
					iterator.remove();
				}
			}
		}
		int totalHours = getTotalHours(usageBlocks);

		log.info("Total unused hours scheduled for instrumentOperator (" + operator.getID() + ", " + operator.getFullName() + ")  at " + now + ": " + totalHours);
		return totalHours;
	}

	private static int getTotalHours(List<? extends UsageBlockBase> blocksBeingScheduled)
	{
		int totalHours = 0;
		for(UsageBlockBase block: blocksBeingScheduled) {

			Date sDate = block.getStartDate();
			Date eDate = block.getEndDate();
			totalHours += (eDate.getTime() - sDate.getTime()) / (1000 * 60 * 60);
		}
		return totalHours;
	}

	public static final class TimeRequest
	{
		private final int _timeUsed;
		private int _timeRequested;

		public TimeRequest(int scheduledTime, int timeRequested)
		{
			_timeUsed = scheduledTime;
			_timeRequested = timeRequested;
		}

		public int getTimeUsed()
		{
			return _timeUsed;
		}

		public int getTimeRequested()
		{
			return _timeRequested;
		}

		public int getTimeRemaining()
		{
			int remaining = HOURS_QUOTA_BILLED_PROJECT - _timeUsed;
			// return remaining < 0 ? 0: remaining;
			return remaining;
		}

		public boolean valid()
		{
			return getTimeRemaining() >= _timeRequested;
		}
	}

	public static final TimeRequest APPROVED = new TimeRequest(0, 0);


	public boolean subsidizedProjectExceedsQuota(int projectId, User user,
			List<? extends UsageBlockBase> blocksBeingScheduled) throws SQLException {
		
		return subsidizedProjectExceedsQuota(projectId, user, blocksBeingScheduled, null);
	}
	
	public boolean subsidizedProjectExceedsQuota(int projectId, User user,
			List<? extends UsageBlockBase> blocksBeingScheduled, List<? extends UsageBlockBase> ignoreBlocks) throws SQLException {
		
		// If the user is an admin return true
		Groups groupsMan = Groups.getInstance();
		
		if (groupsMan.isMember(user.getResearcher().getID(), "administrators"))
			return false;
		
		// The total requested time for this project
		// should not exceed 24 * 5 hours
		
		Set<Integer> ignoreBlockIds = new HashSet<Integer>();
		if(ignoreBlocks != null) {
			for(UsageBlockBase blk: ignoreBlocks) {
				ignoreBlockIds.add(blk.getID());
			}
		}
		
		// get all the time scheduled for this project
		List<UsageBlockBase> usageBlocks = UsageBlockBaseDAO.getUsageBlocksForProject(projectId, null, null, false);
		int totalhours = 0;
		for(UsageBlockBase block: usageBlocks)
		{
			if(ignoreBlockIds.contains(block.getID()))
				continue;
			totalhours += block.getHours();
		}
		
		// add up the time for the blocks being scheduled now
		for(UsageBlockBase block: blocksBeingScheduled)
		{
			totalhours += block.getHours();
		}
		
		log.info("Total hours scheduled for free project: "+totalhours);
		return totalhours >= HOURS_QUOTA_FREE_PROJECT;
	}
	
	public boolean startDateApproved(Date startDate, User user) {
		
		// If the user is an admin return true
		Groups groupsMan = Groups.getInstance();
		
		if (groupsMan.isMember(user.getResearcher().getID(), "administrators"))
			return true;
		
		Date now = new Date(System.currentTimeMillis());
    	if(now.after(startDate)) {
    		return false;
    	}
    	
    	return true;
	}
}
