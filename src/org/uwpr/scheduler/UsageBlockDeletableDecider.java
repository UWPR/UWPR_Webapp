/**
 * UsageBlockDeletableDecider.java
 * @author Vagisha Sharma
 * Jun 2, 2011
 */
package org.uwpr.scheduler;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.uwpr.costcenter.InvoiceInstrumentUsage;
import org.uwpr.costcenter.InvoiceInstrumentUsageDAO;
import org.uwpr.instrumentlog.UsageBlockBase;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;

/**
 * 
 */
public class UsageBlockDeletableDecider {

	private static final UsageBlockDeletableDecider instance = new UsageBlockDeletableDecider();

	private static final String NOT_DELETABLE_EXPIRED = "Selected start time is before the current date, and cannot be changed.";
	private static final String NOT_DELETABLE = "Selected start time is within the next 48 hours.  It cannot be changed or deleted.";

	private UsageBlockDeletableDecider () {}
	
	public static UsageBlockDeletableDecider getInstance() {
		return instance;
	}

	public boolean isBlockEditable(UsageBlockBase block, User user, StringBuilder errorMessage) throws SQLException  {

		// Get the user
		Groups groupsMan = Groups.getInstance();

		// If this block has already been billed it cannot be edited even by admins
		InvoiceInstrumentUsage billedBlock = InvoiceInstrumentUsageDAO.getInstance().getInvoiceBlock(block.getID());
		if(billedBlock != null) {
			errorMessage.append("Block cannot be edited. It has already been billed.");
			return false;
		}

		// Admins can edit the block
		if (groupsMan.isMember(user.getResearcher().getID(), "administrators"))
			return true;


		// If the block was created before the current date it cannot be edited
		if(block.getStartDate().before(new Date(System.currentTimeMillis()))) {
			return false;
		}

		// Usage block can be deleted by a non-admin user only if the start time is atleast
		// 48 hours after the current time
		// UNLESS it was also created within 48 hours of the start time.
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, 48);


		// start time of the block is 48 hours after the current time
		if(block.getStartDate().after(new Date(calendar.getTimeInMillis()))) {
			return true;
		}
		else {
			// If the block was created within the last one hour user should be able to delete it.  They may have made a mistake.
			long numHoursSinceCreate = (System.currentTimeMillis() - block.getDateCreated().getTime()) / (1000*60*60);
			if(numHoursSinceCreate <= 1) {
				return true;
			}
			else {
				errorMessage.append(NOT_DELETABLE);
				return false;
			}
		}
	}

	public boolean isBlockDeletable(UsageBlockBase block, User user, StringBuilder errorMessage) throws SQLException  {

		// If this block has already been billed it cannot be deleted even by admins
		InvoiceInstrumentUsage billedBlock = InvoiceInstrumentUsageDAO.getInstance().getInvoiceBlock(block.getID());
		if(billedBlock != null) {
			errorMessage.append("Block cannot be deleted. It has already been billed.");
			return false;
		}

		// If the user is an admin return true
		Groups groupsMan = Groups.getInstance();
		if (groupsMan.isMember(user.getResearcher().getID(), "administrators"))
			return true;
		
		// If the block was created before the current date it cannot be deleted
		if(block.getStartDate().before(new Date(System.currentTimeMillis()))) {
			errorMessage.append(NOT_DELETABLE_EXPIRED);
			return false;
		}
		
		// Usage block can be deleted by a non-admin user only if the start time is atleast 
		// 48 hours after the current time
		// UNLESS it was also created within the last one hour.
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, 48);
		
		
		// start time of the block is 48 hours after the current time
		if(block.getStartDate().after(new Date(calendar.getTimeInMillis()))) {
			return true;
		}
		else {
			// If the block was created within the last one hour user should be able to delete it.  They may have made a mistake.
			long numHoursSinceCreate = (System.currentTimeMillis() - block.getDateCreated().getTime()) / (1000*60*60);
			if(numHoursSinceCreate <= 1) {
				return true;
			}
			else {
				errorMessage.append(NOT_DELETABLE);
				return false;
			}
		}
	}
}
