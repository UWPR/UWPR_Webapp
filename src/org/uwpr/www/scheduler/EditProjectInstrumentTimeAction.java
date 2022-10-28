/**
 * EditProjectInstrumentTimeFormAction.java
 * @author Vagisha Sharma
 * Jan 6, 2012
 */
package org.uwpr.www.scheduler;

import org.apache.struts.action.*;
import org.uwpr.costcenter.*;
import org.uwpr.instrumentlog.*;
import org.uwpr.scheduler.*;
import org.uwpr.www.util.TimeUtils;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.*;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

/**
 * 
 */
public class EditProjectInstrumentTimeAction extends Action {

	private static final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			return returnError(mapping, request, "username", 
					new ActionMessage("error.login.notloggedin"), "authenticate");
		}
		
		// Get the form
		EditProjectInstrumentTimeForm editForm = (EditProjectInstrumentTimeForm) form;
		
		// we need a projectID
        int projectId = editForm.getProjectId();
        
		
        // Make sure the user has access to the project
        Project project = null;
        try {
        	project = ProjectFactory.getProject(projectId);
        	
        	if(project == null) {
        		return returnError(mapping, request, "scheduler", 
        				new ActionMessage("error.scheduler.invalidid", "Project with ID: "+projectId+" not found in the database."),
        				"standardHome");
        	}
        }
        catch(Exception e) {
        	return returnError(mapping, request, "scheduler", 
        			new ActionMessage("error.costcenter.load", e.getMessage()),
    				"standardHome");
        }
        try {
        	
        	if(!project.checkAccess(user.getResearcher())) {
        		return returnError(mapping, request, "scheduler", 
        				new ActionMessage("error.costcenter.invalidaccess", 
                        		"User does not have access to edit instrument time for project "+projectId+"."),
        				"viewProject", "?ID="+projectId);
        	}
        }
        catch(Exception e) {
        	return returnError(mapping, request, "scheduler", 
        			new ActionMessage("error.costcenter.load", 
                    		"Project ID: "+projectId+". ERROR: "+e.getMessage()),
    				"viewProject", "?ID="+projectId);
        }
        
        // we need an instrumentID
        int instrumentId = editForm.getInstrumentId();
        MsInstrument instrument = null;
        try {
        	instrument = MsInstrumentUtils.instance().getMsInstrument(instrumentId);
        	if(instrument == null) {
        		return returnError(mapping, request, "scheduler", 
        				new ActionMessage("error.scheduler.invalidid", "Instrument with ID "+instrumentId+" not found in the database."),
        				"viewProject", "?ID="+projectId);
        	}
        }
        catch(Exception e) {
        	return returnError(mapping, request, "scheduler", 
        			new ActionMessage("error.costcenter.load", 
                    		"Instrument ID: "+projectId+". ERROR: "+e.getMessage()),
    				"viewProject", "?ID="+projectId);
        }

		// We need an instrument operator
		int instrumentOperatorId = editForm.getInstrumentOperatorId();
		if(instrumentOperatorId == 0)
		{
			return returnError(mapping, request, "scheduler",
					new ActionMessage("error.scheduler.invalidid",
							"Ivalid instrument operator ID: "+instrumentOperatorId),
					        "viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId
					);
		}
		Researcher instrumentOperator = new Researcher();
		try
		{
			instrumentOperator.load(instrumentOperatorId);
		}
		catch(Exception e)
		{
			return returnError(mapping, request, "scheduler",
					new ActionMessage("error.scheduler.invalidid",
							" No researcher found for ID " + instrumentOperatorId),
					"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId
			);
		}

		if(!Groups.getInstance().isMember(instrumentOperatorId, Groups.INSTRUMENT_OPERATOR))
		{
			return returnError(mapping, request, "scheduler",
					new ActionMessage("error.scheduler.invalidid",
							instrumentOperator.getFullName() + " is not a verified instrument operator."),
					"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId
			);
		}

        // set the year and month so that the scheduler initializes at the correct year and month.
        Calendar startCal = Calendar.getInstance();
        Date startDate = editForm.getStartDateDate();
        if(startDate != null)
        {
            startCal.setTime(startDate);
            request.getSession().setAttribute("scheduler_year", startCal.get(Calendar.YEAR));
            request.getSession().setAttribute("scheduler_month", startCal.get(Calendar.MONTH));
        }


		// get the usageBlockIds from the request
        List<Integer> usageBlockIds = new ArrayList<Integer>();
        String usageBlockIdString = editForm.getUsageBlockIdsToEdit();
        if(usageBlockIdString != null) {
        	String[] tokens = usageBlockIdString.split(",");
            for(String token: tokens) {

            	try {
            		usageBlockIds.add(Integer.parseInt(token));
            	}
            	catch(NumberFormatException e) {
            		return returnError(mapping, request, "scheduler", 
            				new ActionMessage("error.costcenter.invaliddata", 
                            		"Invalid usage block ID found in request: "+token+"."),
            				"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
            	}
            }
        }
        if(usageBlockIds.size() == 0) {
        	return returnError(mapping, request, "scheduler", 
        			new ActionMessage("error.costcenter.invaliddata", 
            		"No usage block IDs found in request."),
    				"standardHome");
        }
        
        
        // load the usage blocks
        List<UsageBlockBase> blocksToDelete = new ArrayList<UsageBlockBase>(usageBlockIds.size());
        for(int usageBlockId: usageBlockIds) {
        	
        	UsageBlockBase usageBlock = UsageBlockBaseDAO.getUsageBlockBase(usageBlockId);
        	if(usageBlock == null) {
        		return returnError(mapping, request, "scheduler", 
        				new ActionMessage("error.costcenter.invaliddata", 
        						"No usage block found for usageBlockId: "+usageBlockId),
        						"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
        	}
        	blocksToDelete.add(usageBlock);
        }

		// Sort the blocks by date and make sure they are contiguous
		Collections.sort(blocksToDelete, new Comparator<UsageBlockBase>() {
			@Override
			public int compare(UsageBlockBase o1, UsageBlockBase o2) {
				return o1.getStartDate().compareTo(o2.getStartDate());
			}
		});
		for(int i = 1; i < blocksToDelete.size(); i++)
		{
			if(blocksToDelete.get(i).getStartDate().after(blocksToDelete.get(i -1).getEndDate()))
			{
				return returnError(mapping, request, "scheduler",
						new ActionMessage("error.costcenter.delete",
								"Blocks selected for adjusting time are not contiguous."),
						"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
			}
		}

		// If the user has not edited the dates, just update the instrument operator for the blocks
		Date blksStart = blocksToDelete.get(0).getStartDate();
		Date blksEnd = blocksToDelete.get(blocksToDelete.size() - 1).getEndDate();
		if(blksStart.equals(editForm.getStartDateDate()) && blksEnd.equals(editForm.getEndDateDate()))
		{
			InstrumentUsageDAO.getInstance().updateBlocksInstrumentOperator(blocksToDelete, instrumentOperatorId);
			ActionForward fwd = mapping.findForward("viewScheduler");
			return new ActionForward(fwd.getPath()+"?projectId="+projectId+"&instrumentId="+instrumentId, true);
		}

        // Make sure the old blocks can be deleted.
        for(UsageBlockBase block: blocksToDelete) {
        	StringBuilder errorMessage = new StringBuilder();
        	if(!UsageBlockDeletableDecider.getInstance().isBlockDeletable(block, user, errorMessage)) {
        		return returnError(mapping, request, "scheduler",
        				new ActionMessage("error.costcenter.delete",
        						"Block ID "+block.getID()+": "+block.getStartDateFormated()+" - "+block.getEndDateFormated()+
        						". "+errorMessage.toString()),
        						"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
        	}
        }

		// Get the rate type -- UW, non-profit, commercial etc.
        RateType rateType = project.getRateType();
		if(rateType == null) {
			return returnError(mapping, request, "scheduler",
					new ActionMessage("error.costcenter.invaliddata", "Could not find rate type for project(" + project.getID() + "): " + project.getTitle()),
					"viewScheduler", "?projectId=" + projectId + "&instrumentId=" + instrumentId);
		}
        
        List<UsageBlockBaseWithRate> allBlocks = new ArrayList<>();

		TimeBlock timeBlock = TimeBlockDAO.getInstance().getTimeBlockForName(TimeBlock.HOURLY);
		if(timeBlock == null)
		{
			return returnError(mapping, request, "scheduler",
					new ActionMessage("error.costcenter.invaliddata", "Could not find the hourly time block."),
					"viewScheduler", "?projectId=" + projectId + "&instrumentId=" + instrumentId);
		}

		// get the instrumentRateID
		InstrumentRate rate = InstrumentRateDAO.getInstance().getInstrumentCurrentRate(instrumentId, timeBlock.getId(), rateType.getId());
		if(rate == null) {
			return returnError(mapping, request, "scheduler",
					new ActionMessage("error.costcenter.invaliddata",
							"No rate information found for instrumentId: "+instrumentId+
									" and timeBlockId: "+timeBlock.getId()+" and rateTypeId: "+rateType.getId()+" in request"),
					"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
		}

		// Make sure we can create new blocks in the given time range
		Date rangeStartDate = editForm.getStartDateDate();
		Date rangeEndDate = editForm.getEndDateDate();

		// Check dates
		if(!rangeEndDate.after(rangeStartDate)) {
			String msg = "End date has to be after start date. ";
			msg += "Selected start date was: "+format.format(rangeStartDate)+". ";
			msg += "Selected end date was: "+format.format(rangeEndDate);
			return returnError(mapping, request, "scheduler",
								new ActionMessage("error.costcenter.invaliddata", msg),
						       "viewScheduler", "?projectId=" + projectId + "&instrumentId=" + instrumentId);
		}

		// Make sure the start date is on or after the current date
		if (!ProjectInstrumentTimeApprover.getInstance().startDateApproved(rangeStartDate, user)) {
			return returnError(mapping, request, "scheduler",
					new ActionMessage("error.costcenter.invaliddata",
							"Cannot schedule instrument time in the past!"),
					"viewScheduler", "?projectId=" + projectId + "&instrumentId=" + instrumentId);
		}

		UsageBlockBase oldBlock = blocksToDelete.get(0);

		UsageBlockBaseWithRate usageBlock = new UsageBlockBaseWithRate();
		usageBlock.setProjectID(projectId);
		usageBlock.setInstrumentID(instrumentId);
		usageBlock.setInstrumentOperatorId(instrumentOperatorId);
		usageBlock.setInstrumentRateID(rate.getId());
		usageBlock.setResearcherID(oldBlock.getResearcherID());
		usageBlock.setUpdaterResearcherID(user.getResearcher().getID());
		usageBlock.setDateCreated(oldBlock.getDateCreated());
		usageBlock.setStartDate(rangeStartDate);
		usageBlock.setEndDate(rangeEndDate);
		usageBlock.setRate(rate);
		allBlocks.add(usageBlock);


    	// Make sure user has not exceeded instrument time quota
        if(project instanceof Collaboration) {
            try {
                if(ProjectInstrumentTimeApprover.getInstance().subsidizedProjectExceedsQuota(projectId, user, allBlocks, blocksToDelete)) {
                    return returnError(mapping, request, "scheduler",
                            new ActionMessage("error.costcenter.invaliddata",
                            "You have exceeded the allowed limit. "+
                            "You can only schedule upto "+ProjectInstrumentTimeApprover.HOURS_QUOTA_FREE_PROJECT+
                            " hours of instrument time for a subsidized project."),
                            "viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
                }
            }
            catch(Exception e) {
                return returnError(mapping, request, "scheduler",
                        new ActionMessage("error.costcenter.invaliddata",
                                "Error getting approval for instrument time."+e.getMessage()),
                        "viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
            }
        }
		else if(project instanceof BilledProject)
		{
			ProjectInstrumentTimeApprover.TimeRequest timeRequest = ProjectInstrumentTimeApprover.getInstance().processTimeRequest(user, instrumentOperator, allBlocks, blocksToDelete);
			if(!timeRequest.valid())
			{
				String message = "Could not schedule instrument time." +
						" Requested instrument time exceeds the limit of  " + ProjectInstrumentTimeApprover.HOURS_QUOTA_BILLED_PROJECT + " for " +
						instrumentOperator.getFullName() +
						". Total unused time scheduled on all instruments: " + timeRequest.getTimeUsed() + "  hours." +
						" Time remaining: " + timeRequest.getTimeRemaining() + " hours. ";

				return returnError(mapping, request, "scheduler", new ActionMessage(message),
						"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
			}
		}

        // Check if the instrument is available
    	for(UsageBlockBase block: allBlocks) {
    		if(!InstrumentAvailabilityChecker.getInstance().isInstrumentAvailable(instrumentId, 
    														block.getStartDate(), block.getEndDate(),
    														usageBlockIds)) {
    			return returnError(mapping, request, "scheduler", 
            			new ActionMessage("error.costcenter.invaliddata", 
            					"Instrument is busy at the requested time between "
            					+block.getStartDateFormated() + " and "+block.getEndDateFormated()),
    							"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
    		}
    	}

        // Save the new blocks
    	if(project instanceof BilledProject) {

    		// get the payment method(s) used for the first block.  
    		InstrumentUsagePaymentDAO paymentDao = InstrumentUsagePaymentDAO.getInstance();
    		List<InstrumentUsagePayment> paymentMethods = paymentDao.getPaymentsForUsage(oldBlock.getID());
    		Map<Integer, String> paymentInfoMap = new HashMap<Integer, String>();
    		for(InstrumentUsagePayment paymentMethod: paymentMethods) {
    			paymentInfoMap.put(paymentMethod.getPaymentMethod().getId(), paymentMethod.getPercent().toString());
    		}
    		
    		// Other blocks should also have the same payment method(s).
    		for(int i = 1; i < blocksToDelete.size(); i++) {
    			List<InstrumentUsagePayment> blkPaymentMethods = paymentDao.getPaymentsForUsage(blocksToDelete.get(i).getID());
    			
    			if(blkPaymentMethods.size() != paymentMethods.size()) {
    				return returnError(mapping, request, "scheduler", 
                			new ActionMessage("error.costcenter.invaliddata", 
                					"Number of payment method(s) for all the blocks selected are not the same. "+
                					" Blocks with different payment methods cannot be edited together."),
                					"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
    			}
    			
    			for(InstrumentUsagePayment payment: blkPaymentMethods) {
    				
    				int paymentMethodId = payment.getPaymentMethod().getId();
    				String percent = payment.getPercent().toString();
    				
    				if(!paymentInfoMap.containsKey(paymentMethodId) || !paymentInfoMap.get(paymentMethodId).equals(percent)) {
    					
    					return returnError(mapping, request, "scheduler", 
                    			new ActionMessage("error.costcenter.invaliddata", 
                    					"Payment method(s) for all the blocks selected are not the same. "+
                    					" Blocks with different payment methods cannot be edited together."),
                    					"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
    					
    				}
    			}
    		}
    		
    		UsageBlockPaymentInformation paymentInfo = new UsageBlockPaymentInformation(projectId);
    		// add the payment methods to the request
    		for(Integer paymentMethodId: paymentInfoMap.keySet()) {
				try
				{
					paymentInfo.add(String.valueOf(paymentMethodId), paymentInfoMap.get(paymentMethodId).toString(), rangeEndDate);
				}
				catch(SchedulerException e)
				{
					return returnError(mapping, request, "scheduler",
							new ActionMessage("error.costcenter.invaliddata", e.getMessage()),
							"viewEditInstrumentTimeForm", "?projectId="+projectId+"&instrumentId="+instrumentId+"&usageBlockIds="+usageBlockIdString);
				}
    		}

			Connection conn = null;
			InstrumentUsageDAO instrumentUsageDAO = InstrumentUsageDAO.getInstance();
			try {
				conn = DBConnectionManager.getMainDbConnection();
				conn.setAutoCommit(false);

				// 10.28.2022 - We don't have to keep the blocks in the database since we are no longer billing sign-up fee for deleted blocks.
				// Delete the old blocks
				try {
					instrumentUsageDAO.deletedByEditAction(conn, blocksToDelete, user.getResearcher());
				}

				catch (Exception e) {
					return returnError(mapping, request, "scheduler",
							new ActionMessage("error.costcenter.delete", e.getMessage()),
							"viewScheduler", "?projectId=" + projectId + "&instrumentId=" + instrumentId);
				}

				// If there is no previously scheduled block adjacent to the new first block, make it the setup block
				if(!UsageBlockBaseDAO.hasUsageBlockEndsAt(conn, projectId, instrumentId, allBlocks.get(0).getStartDate()))
				{
					allBlocks.get(0).setSetupBlock(true);
				}

				// Save the blocks
				String errorMessage = instrumentUsageDAO.saveUsageBlocksByEditAction(conn, allBlocks, paymentInfo);
				if (errorMessage != null)
					return returnError(mapping, request, "scheduler",
							new ActionMessage("error.costcenter.invaliddata", errorMessage),
							"viewScheduler", "?projectId=" + projectId + "&instrumentId=" + instrumentId);


				// If there is a setup block adjacent to the last block, remove the setup flag from that block
				UsageBlockBase adjBlock = UsageBlockBaseDAO.getUsageBlockStartsAt(conn, projectId, instrumentId, allBlocks.get(allBlocks.size() - 1).getEndDate());
				if(adjBlock != null)
				{
					InstrumentUsageDAO.getInstance().removeSetupFlag(conn, user.getResearcher(), Collections.singletonList(adjBlock.getID()));
				}

				// Delete and/or adjust any sign-up only blocks that overlap with the new usage blocks
				// 10.28.2022 - No longer need this since we the older blocks are fully deleted.
				// InstrumentUsageDAO.getInstance().deleteOrAdjustSignupBlocks(conn, user.getResearcher(), projectId, instrumentId, rateType, rangeStartDate, rangeEndDate);

				conn.commit();
			}
			catch(Exception e)
			{
				return returnError(mapping, request, "scheduler",
						new ActionMessage("error.costcenter.invaliddata", "There was an error saving changes to usage blocks. " + e.getMessage()),
						"viewScheduler", "?projectId=" + projectId + "&instrumentId=" + instrumentId);
			}
			finally
			{
				if(conn != null) try {conn.close();} catch(SQLException ignored){}
			}

			// Email admins
			boolean emailProjectMembers = !Groups.getInstance().isAdministrator(user.getResearcher());
			String actionMessage = "Editing scheduled time from " + TimeUtils.format(blocksToDelete.get(0).getStartDate()) + " - " + TimeUtils.format(blocksToDelete.get(blocksToDelete.size() - 1).getEndDate());
			ProjectInstrumentUsageUpdateEmailer.getInstance().sendEmail(project, instrument, user.getResearcher(),
					allBlocks, paymentInfo,
					ProjectInstrumentUsageUpdateEmailer.Action.EDITED, actionMessage, emailProjectMembers);
		}
		else {
			// We no longer support subsidized projects
			return returnError(mapping, request, "scheduler",
					new ActionMessage("error.costcenter.invaliddata", "Subsized projects are not supported."),
					"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);

		}

        ActionForward fwd = mapping.findForward("viewScheduler");
        return new ActionForward(fwd.getPath()+"?projectId="+projectId+"&instrumentId="+instrumentId, true);
	}

	private ActionForward returnError(ActionMapping mapping,
			HttpServletRequest request, String errProperty, ActionMessage message, String forward) {
		ActionErrors errors = new ActionErrors();
		errors.add(errProperty, message);
		saveErrors( request, errors );
		return mapping.findForward(forward);
	}
	
	private ActionForward returnError(ActionMapping mapping,
			HttpServletRequest request, String errProperty, ActionMessage message, 
			String forward, String appendToFwdPath) {
		ActionErrors errors = new ActionErrors();
		errors.add(errProperty, message);
		saveErrors( request, errors );
		ActionForward fwd = mapping.findForward(forward);
        return new ActionForward(fwd.getPath()+appendToFwdPath, fwd.getRedirect());
	}
}
