/**
 * 
 */
package org.uwpr.www.scheduler;

import org.apache.log4j.Logger;
import org.apache.struts.action.*;
import org.uwpr.instrumentlog.*;
import org.uwpr.www.util.TimeUtils;
import org.yeastrc.project.BilledProject;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.project.Researcher;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * ViewTimeScheduledForProject.java
 * @author Vagisha Sharma
 * May 31, 2011
 * 
 */
public class ViewTimeScheduledForProject extends Action {

    private static final Logger log = Logger.getLogger(ViewTimeScheduledForProject.class);

    private static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

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
		
        
        // we need a projectID
        int projectId;
        try {
        	projectId = Integer.parseInt(request.getParameter("projectId"));
        }
        catch(NumberFormatException e) {
        	projectId = 0;
        }
        if(projectId == 0) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.scheduler.invalidid", "Invalid projectID in request"));
            saveErrors( request, errors );
            return mapping.findForward("standardHome");
        }
        
        // Make sure the user has access to the project
        Project project;
        try {
        	project = ProjectFactory.getProject(projectId);
        	if(!project.checkAccess(user.getResearcher())) {
        		ActionErrors errors = new ActionErrors();
        		errors.add("scheduler", new ActionMessage("error.scheduler.invalidaccess","User does not have access to project ID " + projectId));
        		saveErrors( request, errors );
            	return mapping.findForward("standardHome");
        	}
        }
        catch(Exception e) {
        	ActionErrors errors = new ActionErrors();
			errors.add("scheduler", new ActionMessage("error.scheduler.load","Error loading project to check access."));
			saveErrors( request, errors );
			log.error("Error checking access to project ID: "+projectId, e);
			ActionForward fwd = mapping.findForward("Failure");
            return new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
        }

        int instrumentId;
        try {
            instrumentId = Integer.parseInt(request.getParameter("instrumentId"));
        }
        catch(NumberFormatException e) {
            instrumentId = 0;
        }
        int paymentMethodId;
        try {
            paymentMethodId = Integer.parseInt(request.getParameter("paymentMethodId"));
        }
        catch(NumberFormatException e) {
            paymentMethodId = 0;
        }

        int instrumentOperatorId;
        try {
            instrumentOperatorId = Integer.parseInt(request.getParameter("instrumentOperatorId"));
        }
        catch(NumberFormatException e) {
            instrumentOperatorId = 0;
        }

        String startDateString = request.getParameter("startDate");
        String endDateString = request.getParameter("endDate");
        Date startDate = null;
        Date endDate = null;
        if(startDateString != null)
        {
            try
            {
                startDate = dateFormat.parse(startDateString);
                startDate = TimeUtils.makeBeginningOfDay(startDate);
            }
            catch(ParseException e)
            {
                log.warn("Error parsing start date "+startDateString+". Setting to null.");
            }

        }
        if(endDateString != null)
        {
            try
            {
                // 2/15/2016 will be parsed as 2016-02-05 00-00-00 (12AM)
                // Add a day (minus 1 millisec.) to the end date so that we include scheduled blocks that start on the end date.
                endDate = dateFormat.parse(endDateString);
                endDate = TimeUtils.makeEndOfDay(endDate);
            }
            catch(ParseException e)
            {
                log.warn("Error parsing end date "+endDateString+". Setting to null.");
            }
        }
        if(endDate != null && startDate != null && endDate.before(startDate)) {
            log.warn("Start date cannot be after end date. Selected start date: "+startDateString + ". End date: "+endDateString);
        }

        UsageBlockFilter filter = new UsageBlockFilter();
        filter.setProjectId(projectId);
        filter.setInstrumentId(instrumentId);
        filter.setInstrumentOperatorId(instrumentOperatorId);
        filter.setPaymentMethodId(paymentMethodId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setContainedInRange(false);
        filter.setTrimToFit(false);
        filter.setBlockType(UsageBlockBaseFilter.BlockType.ALL); // We want both types of blocks (sign-up only AND sign-up + instrument usage)

        ((TimeScheduledFilterForm)form).setFilterCriteria(filter);

        List<UsageBlock> usageBlocks = UsageBlockDAO.getUsageBlocks(filter);
        // sort the blocks by instrument and then by start date, descending
        Collections.sort(usageBlocks, new Comparator<UsageBlock>()
        {
            @Override
            public int compare(UsageBlock o1, UsageBlock o2)
            {
                int val = Integer.valueOf(o1.getInstrumentID()).compareTo(o2.getInstrumentID());
                if (val == 0)
                    return o1.getStartDate().compareTo(o2.getStartDate());
                else
                    return val;
            }
        });
        
        request.setAttribute("project", project);
        request.setAttribute("usageBlocks", usageBlocks);

        int totalSignupHours = 0;
        int totalInstrumentHours = 0;
        BigDecimal signupCost = BigDecimal.ZERO;
        BigDecimal instrumentCost = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for(UsageBlock block: usageBlocks)
        {
            int hours = block.getHours();
            totalCost = totalCost.add(block.getTotalCost());
            signupCost = signupCost.add(block.getSignupCost());
            instrumentCost = instrumentCost.add(block.getInstrumentCost());

            totalSignupHours += hours;

            if(!block.isDeleted())
            {
                totalInstrumentHours += hours;
            }
        }
        request.setAttribute("totalCost", totalCost.setScale(2, RoundingMode.HALF_UP));
        request.setAttribute("signupCost", signupCost.setScale(2, RoundingMode.HALF_UP));
        request.setAttribute("instrumentCost", instrumentCost.setScale(2, RoundingMode.HALF_UP));
        request.setAttribute("signupHours", totalSignupHours);
        request.setAttribute("instrumentHours", totalInstrumentHours);

        List<MsInstrument> instrumentList = MsInstrumentUtils.instance().getMsInstruments();
        List<PaymentMethod> paymentMethods;
        if(project instanceof BilledProject)
        {
            paymentMethods = ((BilledProject)project).getPaymentMethods();
        }
        else
        {
            paymentMethods = Collections.emptyList();
        }
        request.setAttribute("instruments", instrumentList);
        request.setAttribute("paymentMethods", paymentMethods);

        // Put a list of instrument operators listed on the project.
        List<Researcher> allInstrumentOperators = Groups.getInstance().getMembers(Groups.INSTRUMENT_OPERATOR);
        request.setAttribute("instrumentOperators", project.getInstrumentOperators(allInstrumentOperators));

        if(usageBlocks.size() == 0
           && instrumentId == 0
           && paymentMethodId == 0
           && startDate == null
           && endDate == null)
        {
            request.setAttribute("noInstrumentTimeScheduled", true);
        }
        return mapping.findForward("Success");
	}
}
