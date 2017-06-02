/**
 * RequestProjectInstrumentTimeAjaxAction.java
 * @author Vagisha Sharma
 * May 28, 2011
 */
package org.uwpr.www.scheduler;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.uwpr.costcenter.*;
import org.uwpr.instrumentlog.InstrumentUsageDAO;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.MsInstrumentUtils;
import org.uwpr.instrumentlog.UsageBlockBase;
import org.uwpr.scheduler.*;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.*;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.*;

/**
 * 
 */
public class RequestProjectInstrumentTimeAjaxAction extends Action{

	private static final Logger log = Logger.getLogger(RequestProjectInstrumentTimeAjaxAction.class);
	
	private static final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}
		
		
		response.setContentType("application/json");
		
        // we need a projectID
        int projectId = 0;
        try {
        	projectId = Integer.parseInt(request.getParameter("projectId"));
        }
        catch(NumberFormatException e) {
        	projectId = 0;
        }
        if(projectId == 0) {
        	return sendError(response, "Invalid projectID: " + projectId + " in request");
        }
        
        // Make sure the user has access to the project
        Project project = null;
        try {
        	project = ProjectFactory.getProject(projectId);
        	if(!project.checkAccess(user.getResearcher())) {
        		return sendError(response,"User does not have access to schedule instrument time for project");
        	}
        }
        catch(Exception e) {
        	log.error("Error checking access to project ID: "+projectId, e);
        	return sendError(response,"Error loading project to check access. "+e.getMessage());
        }
        
        
        // we need an instrumentID
        int instrumentId = 0;
        try {
        	instrumentId = Integer.parseInt(request.getParameter("instrumentId"));
        }
        catch(NumberFormatException e) {
        	instrumentId = 0;
        }
        
        // Get the instrument operator.
		String instrOperatorParam = request.getParameter("instrumentOperatorId");
		Researcher instrumentOperator = new Researcher();
		try {

			int instrumentOperatorId = Integer.parseInt(instrOperatorParam);
			try
			{
				instrumentOperator.load(instrumentOperatorId);
			}
			catch(Exception e)
			{
				log.error("Error getting researcher object for instrument operator ID: "+instrumentOperatorId, e);
				return sendError(response,"Error getting instrument operator for ID: " + instrumentOperatorId);
			}
		}
		catch(NumberFormatException e) {
			log.error("Invalid instrument operator ID in request: "+instrOperatorParam, e);
			return sendError(response,"Invalid instrument operator ID (" + instrOperatorParam + ") in request.");
		}


        // get a list of ms instruments
		MsInstrument instrument = null;
        List <MsInstrument> instruments = MsInstrumentUtils.instance().getMsInstruments();
        Collections.sort(instruments, new Comparator<MsInstrument>() {
            public int compare(MsInstrument o1, MsInstrument o2) {
                return o1.getID() > o2.getID() ? 1 : (o1.getID() == o2.getID() ? 0 : -1);
        }});

        for(MsInstrument i: instruments) {
        	if(i.getID() == instrumentId)
			{
        		instrument = i;
        		break;
        	}
        }
        if(instrument == null) {
        	return sendError(response,"Invalid instrumentID: "+instrumentId+" in request");
        }
        
        
        
        // If this is a BilledProject make sure it is not blocked
        if(project instanceof BilledProject && ((BilledProject)project).isBlocked()) {
        	return sendError(response,"This project has been blocked. If you think this is in error please contact us.");
        }
        
        
        // Get the rate type -- UW, non-profit, commercial etc.
        RateType rateType = project.getRateType();
		if(rateType == null) {
			return sendError(response,"Could not find rate type for project(" + project.getID() + "): " + project.getTitle());
		}
        
        // Get the start and end date and time
        String startDate = request.getParameter("startDate");
        String startTime = request.getParameter("startTime");
        String endDate = request.getParameter("endDate");
        String endTime = request.getParameter("endTime");
        
        
        Date rangeStartDate = null;
        try {
        	rangeStartDate = PatternToDateConverter.convert(startDate, startTime);
        }
        catch(SchedulerException e) {
        	return sendError(response,"Error reading start date. Error was: "+e.getMessage());
        }
        
        Date rangeEndDate = null;
        try {
        	rangeEndDate = PatternToDateConverter.convert(endDate, endTime);
        }
        catch(SchedulerException e) {
        	return sendError(response,"Error reading end date. Error was: "+e.getMessage());
        }
        
        // Split the given range into time blocks
        List<TimeBlock> timeBlocks = TimeBlockDAO.getInstance().getAllTimeBlocks();
        List<TimeBlock> rangeTimeBlocks = null;
        try {
        	rangeTimeBlocks = TimeRangeSplitter.getInstance().split(rangeStartDate, rangeEndDate, timeBlocks);
        }
        catch(SchedulerException e) {
        	return sendError(response,"Could not convert given time range into blocks. Error was: "+e.getMessage());
        }
        if(rangeTimeBlocks == null || rangeTimeBlocks.size() == 0) {
        	if(!rangeEndDate.after(rangeStartDate)) {
        		String msg = "End date has to be after start date. ";
        		msg += "Selected start date was: "+format.format(rangeStartDate)+". ";
        		msg += "Selected end date was: "+format.format(rangeEndDate);
        		return sendError(response,msg);
        	}
        	else {
        		return sendError(response,"Could not convert given time range into blocks.");
        	}
        }
    	
    	List<UsageBlockBaseWithRate> allBlocks = new ArrayList<UsageBlockBaseWithRate>();
    	
    	boolean first = true;
    	Calendar startCal = Calendar.getInstance();
    	startCal.setTime(rangeStartDate);
    	
    	for(TimeBlock timeBlock: rangeTimeBlocks) {
    		
    		// get the instrumentRateID
            InstrumentRate rate = InstrumentRateDAO.getInstance().getInstrumentCurrentRate(instrumentId, timeBlock.getId(), rateType.getId());
            if(rate == null) {
            	return sendError(response,"No rate information found for instrumentId: "+instrumentId+
            			" and timeBlockId: "+timeBlock.getId()+" and rateTypeId: "+rateType.getId()+" in request");
            }
            
            UsageBlockBaseWithRate usageBlock = new UsageBlockBaseWithRate();
            usageBlock.setProjectID(projectId);
            usageBlock.setInstrumentID(instrumentId);
			usageBlock.setInstrumentOperatorId(instrumentOperator.getID());
            usageBlock.setInstrumentRateID(rate.getId());
            usageBlock.setResearcherID(user.getResearcher().getID());
            usageBlock.setStartDate(startCal.getTime());
            startCal.add(Calendar.HOUR_OF_DAY, timeBlock.getNumHours());
            usageBlock.setEndDate(startCal.getTime());
            usageBlock.setRate(rate);
            
            
            if(first) {
            	// Make sure the start date is on or after the current date
            	if(!ProjectInstrumentTimeApprover.getInstance().startDateApproved(usageBlock.getStartDate(), user)) {
            		return sendError(response, "Cannot schedule instrument time in the past!");
            	}
            	
            	first = false;
            }
            
            allBlocks.add(usageBlock);
            	
    	}
    	
		// Check if the instrument is available
    	for(UsageBlockBase block: allBlocks) {
    		if(!InstrumentAvailabilityChecker.getInstance().isInstrumentAvailable(instrumentId, block.getStartDate(), block.getEndDate())) {

    			return sendError(response, "Instrument is busy at the requested time between "
    					+block.getStartDateFormated() + " and "+block.getEndDateFormated());
    		}
    	}
		
    	// Make sure user has not exceeded instrument time quota
        try {
        	if(project instanceof BilledProject)
			{
				ProjectInstrumentTimeApprover.TimeRequest timeRequest = ProjectInstrumentTimeApprover.getInstance().processTimeRequest(user, instrumentOperator, allBlocks);

        		if(!timeRequest.valid())
				{
        			return sendError(response,"Could not schedule instrument time." +
							" Requested instrument time exceeds the limit of  " + ProjectInstrumentTimeApprover.HOURS_QUOTA_BILLED_PROJECT + " for " +
							instrumentOperator.getFullName() +
        					". Total unused time scheduled on all instruments: " + timeRequest.getTimeUsed() + "  hours." +
					        " Time remaining: " + timeRequest.getTimeRemaining() + " hours. ");
        		}
        	}
        	else if(project instanceof Collaboration)
			{
        		if(ProjectInstrumentTimeApprover.getInstance().subsidizedProjectExceedsQuota(projectId, user, allBlocks))
				{
        			return sendError(response,"You have exceeded the allowed limit. "+
        					"You can only schedule upto "+ProjectInstrumentTimeApprover.HOURS_QUOTA_FREE_PROJECT+
        			" hours of instrument time for a subsidized project.");
        		}
        	}
        }
        catch(Exception e) {
        	log.error("Error getting approval for instrument time.", e);
        	return sendError(response,"Error getting approval for instrument time."+e.getMessage());
        }

		boolean requiresConfirmation = true;
		String requiresConfStr = request.getParameter("requiresConfirmation");
		if(!StringUtils.isBlank(requiresConfStr))
		{
			requiresConfirmation = Boolean.parseBoolean(requiresConfStr);
		}

        if(!requiresConfirmation)
		{
			// Save the blocks
			if (project instanceof BilledProject)
			{
				if(allBlocks == null || allBlocks.size() == 0)
				{
					sendError(response, "No usage blocks found.");
				}

				UsageBlockPaymentInformation paymentInfo = null;
				try
				{
					paymentInfo = getPaymentInfo(request, allBlocks);
					String errorMessage = saveUsageBlocksForBilledProject(allBlocks, paymentInfo, rateType, user.getResearcher());
					if (errorMessage != null)
						return sendError(response, errorMessage);
				}
				catch(SchedulerException | IllegalArgumentException e)
				{
					sendError(response, e.getMessage());
				}

				// Email admins
				boolean emailProjectMembers = !Groups.getInstance().isAdministrator(user.getResearcher());
				ProjectInstrumentUsageUpdateEmailer.getInstance().sendEmail(project, instrument, user.getResearcher(),
						allBlocks, paymentInfo,
						ProjectInstrumentUsageUpdateEmailer.Action.ADDED, null, emailProjectMembers);
			} else
			{
				return sendError(response, "Subsidized projects are not supported.");
			}
		}

		// Write the response
		PrintWriter writer = response.getWriter();
		writer.write(getJSONSuccess(allBlocks, requiresConfirmation));


		return null;
	}

	private static UsageBlockPaymentInformation getPaymentInfo(HttpServletRequest request,
															   List<? extends UsageBlockBase> usageBlocks
															   ) throws SchedulerException {

		Date endDate = usageBlocks.get(usageBlocks.size() - 1).getEndDate();

		// Get the payment method(s)
		UsageBlockPaymentInformation paymentInfo = new UsageBlockPaymentInformation(usageBlocks.get(0).getProjectID());

		String method1IdString = request.getParameter("paymentMethodId1");
		if(method1IdString == null) {
			throw new IllegalArgumentException( "No payment method found in request");
		}
		String method1Perc = request.getParameter("paymentMethod1Percent");
		if(method1Perc == null) {
			throw new IllegalArgumentException("Percent to be billed to payment method 1 not found in request");
		}

		// Throws SchedulerExceptoin
		paymentInfo.add(method1IdString, method1Perc, endDate);

		if(request.getParameter("paymentMethodId2") != null && !(request.getParameter("paymentMethodId2").equals("0"))) {

			if(request.getParameter("paymentMethod2Percent") == null) {
				throw new IllegalArgumentException("Percent to be billed to payment method 2 not found in request");
			}

			// Throws SchedulerException
			paymentInfo.add(request.getParameter("paymentMethodId2"), request.getParameter("paymentMethod2Percent"), endDate);
		}

		return paymentInfo;
	}

	private static String saveUsageBlocksForBilledProject(
			List<? extends UsageBlockBase> usageBlocks,
			UsageBlockPaymentInformation paymentInfo, RateType rateType, Researcher user) {

		Connection conn = null;
		InstrumentUsageDAO instrumentUsageDAO = InstrumentUsageDAO.getInstance();
		try
		{
			conn = DBConnectionManager.getMainDbConnection();
			conn.setAutoCommit(false);

			String errorMessage = instrumentUsageDAO.saveUsageBlocks(conn, usageBlocks, paymentInfo);
			if (errorMessage != null)
			{
				return errorMessage;
			}

			// Update signup
			UsageBlockBase firstBlock = usageBlocks.get(0);
			Date startDate = firstBlock.getStartDate();
			Date endDate = usageBlocks.get(usageBlocks.size() - 1).getEndDate();

			instrumentUsageDAO.deleteOrAdjustSignupBlocks(conn, user, firstBlock.getProjectID(), firstBlock.getInstrumentID(), rateType, startDate, endDate);

			conn.commit();
		}
		catch(Exception e)
		{
			log.error("Error saving usage blocks", e);
			return "There was an error saving usage block. Error was: " + e.getMessage();
		}
		finally
		{
			if(conn != null) try {conn.close();} catch(Exception ignored){}
		}
		return null;
	}

	private ActionForward sendError(HttpServletResponse response, String errorMessage) throws IOException {
		PrintWriter responseWriter = response.getWriter();
		responseWriter.write(getJSONError(errorMessage));
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return null;
	}
	
	private String getJSONError(String errorMessage) {
		
		JSONObject json = new JSONObject();
		json.put("message", errorMessage);
		json.put("response_type", "ERROR");
		return json.toJSONString();
	}
	
	private String getJSONSuccess(List<UsageBlockBaseWithRate> usageBlocks, boolean requiresConfirmation) {
		
		JSONObject json = new JSONObject();
		json.put("response_type", "SUCCESS");
		
		JSONArray array = new JSONArray();
		json.put("blocks", array);
		BigDecimal cost = BigDecimal.ZERO;
		BigDecimal instrumentCost = BigDecimal.ZERO;
		BigDecimal signupCost = BigDecimal.ZERO;

		for(UsageBlockBaseWithRate block: usageBlocks) {
			JSONObject obj = new JSONObject();
			obj.put("id", String.valueOf(block.getID()));
			obj.put("fee", String.valueOf(block.getRate().getRate()));
			obj.put("start_date", block.getStartDateFormated());
			obj.put("end_date", block.getEndDateFormated());
			array.add(obj);

			InstrumentRate rate = block.getRate();
			cost = cost.add(rate.getRate());
			instrumentCost = instrumentCost.add(rate.getInstrumentFee());
			signupCost = signupCost.add(rate.getSignupFee());
		}
		json.put("total_cost", cost.doubleValue());
		json.put("signup_cost", signupCost.doubleValue());
		json.put("instrument_cost", instrumentCost.doubleValue());

		json.put("requires_confirmation", requiresConfirmation);

		// log.info(json.toJSONString());
		return json.toJSONString();
	}
}
