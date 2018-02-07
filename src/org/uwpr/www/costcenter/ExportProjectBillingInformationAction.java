/**
 * ExportBillingInformationAction.java
 * @author Vagisha Sharma
 * Jun 18, 2011
 */
package org.uwpr.www.costcenter;

import org.apache.log4j.Logger;
import org.apache.struts.action.*;
import org.uwpr.costcenter.*;
import org.uwpr.www.util.TimeUtils;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 */
public class ExportProjectBillingInformationAction extends Action {

	private static final Logger log = Logger.getLogger(ExportProjectBillingInformationAction.class);

	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM.dd.yyyy");

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
				// Add a day to the end date.
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

		boolean summarize = request.getParameter("summarize") != null ? true : false;

		//PrintWriter writer = response.getWriter();
		ServletOutputStream outStream = response.getOutputStream();

		// response.setContentType("text/plain");
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition","attachment; filename=\"Billing_"+dateFormatter.format(startDate)+"_TO_"+dateFormatter.format(endDate)+".xls\"");
		response.setHeader("cache-control", "no-cache");

    	try {
        	
        	BillingInformationExcelExporter exporter = new BillingInformationExcelExporter();

        	exporter.setStartDate(startDate);
			exporter.setEndDate(endDate);
        	
        	exporter.setSummarize(summarize);
        	
        	exporter.exportToXls(project, outStream);

        }
        catch(BillingInformationExporterException e) {
        	log.error("Error exporting data", e);
        	response.reset();
        	
        	ActionErrors errors = new ActionErrors();
            errors.add("costcenter", new ActionMessage("error.costcenter.export", e.getMessage()));
            saveErrors( request, errors );
            ActionForward fwd = mapping.findForward("Failure");
			ActionForward newFwd = new ActionForward(fwd.getPath()+"?projectId="+projectId, fwd.getRedirect());
			return newFwd;
        }
        catch(Exception e) {
        	log.error("Exception writing response", e);
        	
        	response.reset();
        	
        	ActionErrors errors = new ActionErrors();
            errors.add("costcenter", new ActionMessage("error.costcenter.export", e.getMessage()));
            saveErrors( request, errors );
			ActionForward fwd = mapping.findForward("Failure");
			ActionForward newFwd = new ActionForward(fwd.getPath()+"?projectId="+projectId, fwd.getRedirect());
			return newFwd;
        }
        
        return null;
	}
}
