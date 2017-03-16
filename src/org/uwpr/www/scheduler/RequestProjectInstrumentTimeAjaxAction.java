/**
 * RequestProjectInstrumentTimeAjaxAction.java
 * @author Vagisha Sharma
 * May 28, 2011
 */
package org.uwpr.www.scheduler;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.uwpr.costcenter.InstrumentRate;
import org.uwpr.costcenter.InstrumentRateDAO;
import org.uwpr.costcenter.RateType;
import org.uwpr.costcenter.RateTypeDAO;
import org.uwpr.costcenter.TimeBlock;
import org.uwpr.costcenter.TimeBlockDAO;
import org.uwpr.instrumentlog.*;
import org.uwpr.scheduler.InstrumentAvailabilityChecker;
import org.uwpr.scheduler.PatternToDateConverter;
import org.uwpr.scheduler.ProjectInstrumentTimeApprover;
import org.uwpr.scheduler.SchedulerException;
import org.uwpr.scheduler.TimeRangeSplitter;
import org.uwpr.scheduler.UsageBlockBaseWithRate;
import org.uwpr.scheduler.UsageBlockPaymentInformation;
import org.uwpr.scheduler.UsageBlockRepeatBuilder;
import org.uwpr.www.costcenter.UwprSupportedProjectPaymentMethodGetter;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.*;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

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
        RateType rateType = null;
        if(project instanceof BilledProject) {
        	Affiliation affiliation = ((BilledProject)project).getAffiliation();
        	rateType = RateTypeDAO.getInstance().getRateTypeForAffiliation(affiliation, project.isMassSpecExpertiseRequested());
        	if(rateType == null) {
            	return sendError(response,"Could not find rate type for affiliation: "+affiliation.name());
            }
        }
        else if(project instanceof Collaboration) {
        	rateType = RateTypeDAO.getInstance().getRateForUwprSupportedProjects();
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
        
        
        // Has the user checked the "Repeat Daily" checkbox?
    	boolean repeatDaily = Boolean.parseBoolean(request.getParameter("repeatdaily"));
    	if(repeatDaily && rangeTimeBlocks.size() > 1) {
    		return sendError(response,"Selected time range cannot be repeated daily.");
    	}
    	if(repeatDaily && rangeTimeBlocks.get(0).getNumHours() > 24) {
    		return sendError(response,"Selected time range exceeds 24 hours and cannot be repeated daily.");
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
       
        
        
        // Is this block being repeated on a daily basis?
    	if(repeatDaily) {
        	
        	String repeatEndDateString = request.getParameter("repeatenddate");
    		if(repeatEndDateString == null || repeatEndDateString.trim().length() == 0) {
    			return sendError(response, "Repeat was checked but no end date was specified");
    		}
    		Date repeatEndDate = null;
    		try {
    			repeatEndDate = PatternToDateConverter.parseDate(repeatEndDateString);
    		}
    		catch(ParseException e) {
    			return sendError(response, "Error parsing repeat end date: "+repeatEndDate);
    		}
    		
    		UsageBlockBaseWithRate block = allBlocks.get(0);
    		try {
    			allBlocks = UsageBlockRepeatBuilder.getInstance().repeatDaily(block, repeatEndDate, rangeTimeBlocks.get(0));
    		}
    		catch(SchedulerException e) {
    			return sendError(response, "Could not create repeating events. Error was:: "+e.getMessage());
    		}
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

		boolean requiresConfirmation = Boolean.parseBoolean(request.getParameter("requiresConfirmation"));


        if(!requiresConfirmation)
		{
			// Save the blocks
			if (project instanceof BilledProject)
			{
				String errorMessage = saveUsageBlocksForBilledProject(request, response, allBlocks, rangeEndDate);
				if (errorMessage != null)
					return sendError(response, errorMessage);
			} else
			{
				String errorMessage = saveUsageBlocksForSubsidizedProject(allBlocks);
				if (errorMessage != null)
					return sendError(response, errorMessage);
			}

			// Email admins
			ProjectInstrumentUsageUpdateEmailer.getInstance().sendEmail(project, instrument, user.getResearcher(),
					allBlocks,
					ProjectInstrumentUsageUpdateEmailer.Action.ADDED);
		}

		// Write the response
		PrintWriter writer = response.getWriter();
		writer.write(getJSONSuccess(allBlocks, requiresConfirmation));


		return null;
	}

	

	public static String saveUsageBlocksForSubsidizedProject(List<? extends UsageBlockBase> usageBlocks) {
		
		List<Integer> savedBlockIds = new ArrayList<Integer>();
		
		PaymentMethod pm = UwprSupportedProjectPaymentMethodGetter.get(usageBlocks.get(0).getProjectID());
		
		if(pm == null) {
			log.error("No payment method was found for UWPR supported projects");
			return "No payment method was found for UWPR supported projects";
		}
		
		InstrumentUsagePaymentDAO iupDao = InstrumentUsagePaymentDAO.getInstance();
		
		for(UsageBlockBase block: usageBlocks) {
			
			log.info("Saving usage block: "+block.toString());
			
			try {
				InstrumentUsageDAO.getInstance().save(block);
				savedBlockIds.add(block.getID());
				
				
				InstrumentUsagePayment usagePayment = new InstrumentUsagePayment();
    			usagePayment.setInstrumentUsageId(block.getID());
    			usagePayment.setPaymentMethod(pm);
    			usagePayment.setPercent(new BigDecimal("100.0"));
    			iupDao.savePayment(usagePayment);
			}
			catch(Exception e) {
        		
        		// delete the usage blocks saved thus far and throw an error
        		for(Integer blockId: savedBlockIds) {
        			try {
        				InstrumentUsageDAO.getInstance().delete(blockId);
        			}
        			catch(Exception ex) {
            			log.error("There was an error deleting block ID: "+blockId);
            		}
        		}
        		
        		log.error("Error saving usage block", e);
        		return "There was an error saving time block. Error was: "+e.getMessage();
        	}
    	}
		
		return null;
	}
	
	private static String saveUsageBlocksForBilledProject(HttpServletRequest request, HttpServletResponse response, 
			List<? extends UsageBlockBase> usageBlocks, Date endDate) throws Exception
	{
		// Get the payment method(s)
		UsageBlockPaymentInformation paymentInfo = new UsageBlockPaymentInformation(usageBlocks.get(0).getProjectID());
		
		String method1IdString = request.getParameter("paymentMethodId1");
		if(method1IdString == null) {
			return "No payment method found in request";
		}
		String method1Perc = request.getParameter("paymentMethod1Percent");
		if(method1Perc == null) {
			return "Percent to be billed to payment method 1 not found in request";
		}
		try {
			paymentInfo.add(method1IdString, method1Perc, endDate);
		}
		catch(SchedulerException e) {
			return e.getMessage();
		}
		
        if(request.getParameter("paymentMethodId2") != null && !(request.getParameter("paymentMethodId2").equals("0"))) {
        	
        	if(request.getParameter("paymentMethod2Percent") == null) {
        		return "Percent to be billed to payment method 2 not found in request";
        	}
        	try {
        		paymentInfo.add(request.getParameter("paymentMethodId2"), request.getParameter("paymentMethod2Percent"), endDate);
        	}
    		catch(SchedulerException e) {
    			return e.getMessage();
    		}
        }

        return saveUsageBlocksForBilledProject(usageBlocks, paymentInfo);
        
	}

	private static String saveUsageBlocksForBilledProject(
			List<? extends UsageBlockBase> usageBlocks,
			UsageBlockPaymentInformation paymentInfo) {

        InstrumentUsagePaymentDAO iupDao = InstrumentUsagePaymentDAO.getInstance();

		Connection conn = null;
		try
		{
			conn = DBConnectionManager.getMainDbConnection();
			conn.setAutoCommit(false);

			saveUsageBlocksForBilledProject(conn, usageBlocks, paymentInfo);

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

	public static String saveUsageBlocksForBilledProject(
			Connection conn,
			List<? extends UsageBlockBase> usageBlocks,
			UsageBlockPaymentInformation paymentInfo) {

		InstrumentUsagePaymentDAO iupDao = InstrumentUsagePaymentDAO.getInstance();

		try
		{
			// Blocks are in order
			for (UsageBlockBase block : usageBlocks)
			{
				log.info("Saving usage block: " + block.toString());

				// save to the instrumentUsage table
				InstrumentUsageDAO.getInstance().save(conn, block);

				for (int i = 0; i < paymentInfo.getCount(); i++)
				{

					PaymentMethod pm = paymentInfo.getPaymentMethod(i);
					BigDecimal perc = paymentInfo.getPercent(i);

					InstrumentUsagePayment usagePayment = new InstrumentUsagePayment();
					usagePayment.setInstrumentUsageId(block.getID());
					usagePayment.setPaymentMethod(pm);
					usagePayment.setPercent(perc);
					iupDao.savePayment(conn, usagePayment);
				}
			}

			updateSignup(conn, usageBlocks, paymentInfo);
		}
		catch(Exception e)
		{
			log.error("Error saving usage blocks", e);
			return "There was an error saving usage block. Error was: " + e.getMessage();
		}

		return null;
	}

	private static void updateSignup(Connection conn, List<? extends UsageBlockBase> usageBlocks, UsageBlockPaymentInformation paymentInfo) throws Exception
	{
		InstrumentSignupDAO signupDao = InstrumentSignupDAO.getInstance();

		// Log to instrumentSignupLog
		signupDao.logInstrumentSignUp(conn, usageBlocks);

		Date startDate = usageBlocks.get(0).getStartDate();
		Date endDate = usageBlocks.get(usageBlocks.size() - 1).getEndDate();

		InstrumentSignup newSignup = new InstrumentSignup();
		UsageBlockBase firstBlock = usageBlocks.get(0);
		newSignup.setProjectId(firstBlock.getProjectID());
		newSignup.setInstrumentID(firstBlock.getInstrumentID());
		newSignup.setStartDate(startDate);
		newSignup.setEndDate(endDate);
		newSignup.setCreatedBy(firstBlock.getResearcherID());

		List<SignupPayment> payments = new ArrayList<SignupPayment>();
		newSignup.setPayments(payments);
		for (int i = 0; i < paymentInfo.getCount(); i++)
        {
            PaymentMethod pm = paymentInfo.getPaymentMethod(i);
            BigDecimal perc = paymentInfo.getPercent(i);

            SignupPayment payment = new SignupPayment();
            payment.setPaymentMethod(pm);
            payment.setPercent(perc);
            payments.add(payment);
        }

		RateType rateType = ((UsageBlockBaseWithRate)usageBlocks.get(0)).getRate().getRateType();
		// Split the given range into time blocks
		// TODO: do we need to do this twice?? We have already done this for the instrument usage blocks
		makeSignupBlocks(newSignup, rateType);
		signupDao.save(conn, newSignup);

		// Check if there is existing overlapping signup (ordered by startDate)
		List<InstrumentSignup> existingSignUps = signupDao.getExistingSignup(startDate, endDate);

		for(InstrumentSignup signup: existingSignUps)
        {
			Date sStartDate = signup.getStartDate();
			Date sEndDate = signup.getEndDate();
			if((sStartDate.equals(startDate) || sStartDate.after(startDate)) &&
			   (sEndDate.equals(endDate) || sEndDate.before(endDate)))
			{
				// delete this signup (it is contained in the new signup request)
				signupDao.deleteSignup(conn, Collections.singletonList(signup));
			}

			else if(sStartDate.before(startDate))
			{
				signup.setEndDate(startDate);
				makeSignupBlocks(signup, rateType);
				signupDao.updateSignup(conn, signup, true, false);
				if(sEndDate.after(endDate))
				{
					// The new signup is contained in the old signup.  Split the old signup into two blocks
					InstrumentSignup signupRight = new InstrumentSignup();
					signupRight.setStartDate(endDate);
					signupRight.setEndDate(signup.getEndDate());
					signupRight.setCreatedBy(signup.getCreatedBy());
					signupRight.setDateCreated(signup.getDateCreated());
					signupRight.setInstrumentID(signup.getInstrumentID());
					signupRight.setProjectId(signup.getProjectID());
					signupRight.setPayments(signup.getPayments());
					makeSignupBlocks(signupRight, rateType);
					signupDao.save(conn, signupRight);
				}
			}

			else if(sEndDate.after(endDate))
			{
				signup.setStartDate(endDate);
				makeSignupBlocks(signup, rateType);
				signupDao.updateSignup(conn, signup, true, false);
			}
			else
			{
				// We should not be here!!
				throw new Exception("Cannot handle existing overlapping signup: " + sStartDate + " to " + sEndDate
				+ ". Requested signup was from " + startDate + " to " + endDate);
			}
        }
	}

	private static void makeSignupBlocks(InstrumentSignupGeneric signup, RateType rateType) throws Exception
	{

		List<TimeBlock> timeBlocks = TimeBlockDAO.getInstance().getAllTimeBlocks();
		List<TimeBlock> rangeTimeBlocks = TimeRangeSplitter.getInstance().split(signup.getStartDate(), signup.getEndDate(), timeBlocks);

		List<SignupBlock> allBlocks = new ArrayList<SignupBlock>();

		Calendar startCal = Calendar.getInstance();
		startCal.setTime(signup.getStartDate());

		int instrumentId = signup.getInstrumentID();

		for(TimeBlock timeBlock: rangeTimeBlocks)
		{
			// get the instrumentRateID
			InstrumentRate rate = InstrumentRateDAO.getInstance().getInstrumentCurrentRate(instrumentId, timeBlock.getId(), rateType.getId());
			if(rate == null) {
				throw new Exception("No rate information found for instrumentId: "+instrumentId+
						" and timeBlockId: "+timeBlock.getId()+" and rateTypeId: "+rateType.getId()+" in request");
			}

			SignupBlock block = new SignupBlock();
			block.setInstrumentSignupId(signup.getId());
			block.setInstrumentRateId(rate.getId());
			block.setStartDate(startCal.getTime());
			startCal.add(Calendar.HOUR_OF_DAY, timeBlock.getNumHours());
			block.setEndDate(startCal.getTime());

			allBlocks.add(block);
		}
		signup.setBlocks(allBlocks);
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
