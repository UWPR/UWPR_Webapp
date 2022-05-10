/**
 * 
 */
package org.uwpr.www.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;
import org.uwpr.instrumentlog.*;
import org.uwpr.www.util.TimeUtils;
import org.yeastrc.project.BilledProject;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ViewTimeScheduledForProject.java
 * @author Vagisha Sharma
 * May 31, 2011
 * 
 */
public class ViewTimeScheduledForOperator extends Action {

    private static final Logger log = LogManager.getLogger(ViewTimeScheduledForOperator.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception
    {

		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}

        // we need an instrument operatorId
        String instrumentOperatorParam = request.getParameter("instrumentOperatorId");
        int instrumentOperatorId = 0;
        try
        {
            instrumentOperatorId = Integer.parseInt(instrumentOperatorParam);
        }
        catch(NumberFormatException e) {}

        if(instrumentOperatorId == 0) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.scheduler.invalidid",
                       "Invalid instrument operator ID (" + instrumentOperatorParam + ") found in request"));
            saveErrors( request, errors );
            return mapping.findForward("standardHome");
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
        int projectId = 0;
        try
        {
            projectId = Integer.parseInt(request.getParameter("projectId"));
        }
        catch(NumberFormatException e){}

        if(projectId > 0)
        {
            // Make sure the user has access to the project
            Project project;
            try
            {
                project = ProjectFactory.getProject(projectId);
                if (!project.checkAccess(user.getResearcher()))
                {
                    ActionErrors errors = new ActionErrors();
                    errors.add("scheduler", new ActionMessage("error.scheduler.invalidaccess", "User does not have access to project ID " + projectId));
                    saveErrors(request, errors);
                    return mapping.findForward("standardHome");
                }
            } catch (Exception e)
            {
                ActionErrors errors = new ActionErrors();
                errors.add("scheduler", new ActionMessage("error.scheduler.load", "Error loading project to check access."));
                saveErrors(request, errors);
                log.error("Error checking access to project ID: " + projectId, e);
                ActionForward fwd = mapping.findForward("Failure");
                ActionForward newFwd = new ActionForward(fwd.getPath() + "?ID=" + projectId, fwd.getRedirect());
                return newFwd;
            }
        }

        String startDateString = request.getParameter("startDate");
        String endDateString = request.getParameter("endDate");
        Date startDate = null;
        Date endDate = null;
        if(startDateString != null)
        {
            try
            {
                startDate = TimeUtils.shortDate.parse(startDateString);
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
                // Add a day to the end date so that we include scheduled blocks that start on the end date.
                endDate = TimeUtils.shortDate.parse(endDateString);
                endDate = TimeUtils.makeEndOfDay_12AM(endDate);
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
        filter.setInstrumentOperatorId(instrumentOperatorId);
        filter.setProjectId(projectId);
        filter.setInstrumentId(instrumentId);
        filter.setPaymentMethodId(paymentMethodId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setTrimToFit(false);
        filter.setContainedInRange(false);

        ((TimeScheduledFilterForm)form).setFilterCriteria(filter);

        List<UsageBlock> usageBlocks = UsageBlockDAO.getUsageBlocks(filter);
        // sort the blocks by instrument and then by start date, descending
        Collections.sort(usageBlocks, new Comparator<UsageBlock>() {
			@Override
			public int compare(UsageBlock o1, UsageBlock o2) {
				int val = Integer.valueOf(o1.getInstrumentID()).compareTo(o2.getInstrumentID());
				if(val == 0)
					return o1.getStartDate().compareTo(o2.getStartDate());
				else
					return val;
			}
		});
        
        request.setAttribute("usageBlocks", usageBlocks);

        int totalHours = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        for(UsageBlock block: usageBlocks)
        {
            totalCost = totalCost.add(block.getTotalCost());
            totalHours += block.getHours();
        }
        request.setAttribute("totalCost", totalCost);
        request.setAttribute("totalHours", totalHours);

        User instrumentOperator = new User();
        instrumentOperator.load(instrumentOperatorId);

        List<MsInstrument> instrumentList = MsInstrumentUtils.instance().getMsInstruments();
        List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
        List<Project> allProjects = instrumentOperator.getProjects();
        List<Project> billedOperatorProjects = new ArrayList<Project>();
        for(Project project: allProjects)
        {
            if(project instanceof BilledProject)
            {
                paymentMethods.addAll(((BilledProject)project).getPaymentMethods());
                billedOperatorProjects.add(project);
            }
        }
        request.setAttribute("projects", billedOperatorProjects);
        request.setAttribute("instruments", instrumentList);
        request.setAttribute("paymentMethods", paymentMethods);
        request.setAttribute("instrumentOperator", instrumentOperator);

        if(billedOperatorProjects.size() == 1)
        {
            ((TimeScheduledFilterForm)form).setProjectId(billedOperatorProjects.get(0).getID());
        }
        if(paymentMethods.size() == 1)
        {
            ((TimeScheduledFilterForm)form).setPaymentMethodId(paymentMethods.get(0).getId());
        }


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
