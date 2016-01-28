/**
 * 
 */
package org.uwpr.www.scheduler;

import org.apache.log4j.Logger;
import org.apache.struts.action.*;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.MsInstrumentUtils;
import org.uwpr.instrumentlog.UsageBlock;
import org.uwpr.instrumentlog.UsageBlockDAO;
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
			ActionForward newFwd = new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
        	return newFwd;
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
                endDate = dateFormat.parse(endDateString);
            }
            catch(ParseException e)
            {
                log.warn("Error parsing end date "+endDateString+". Setting to null.");
            }
        }
        if(endDate != null && startDate != null && endDate.before(startDate)) {
            log.warn("Start date cannot be after end date. Selected start date: "+startDateString + ". End date: "+endDateString);
        }

        ViewTimeScheduledForProjectForm filterForm = (ViewTimeScheduledForProjectForm)form;
        filterForm.setInstrumentId(instrumentId);
        filterForm.setPaymentMethodId(paymentMethodId);
        filterForm.setStartDateString(startDateString);
        filterForm.setEndDateString(endDateString);

        List<UsageBlock> usageBlocks = ProjectInstrumentUsageDAO.getInstance().getUsageBlocksForProject(projectId,
                                                                                                        filterForm.getInstrumentId(),
                                                                                                        filterForm.getPaymentMethodId(),
                                                                                                        startDate,
                                                                                                        endDate);
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
        
        request.setAttribute("project", project);
        request.setAttribute("usageBlocks", usageBlocks);

        int totalHours = 0;
        double totalCost = 0;
        for(UsageBlock block: usageBlocks)
        {
            totalCost += block.getRate().doubleValue();
            totalHours += block.getHours();
        }
        request.setAttribute("totalCost", totalCost);
        request.setAttribute("totalHours", totalHours);

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
