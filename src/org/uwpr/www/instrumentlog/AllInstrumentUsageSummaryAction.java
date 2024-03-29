/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package org.uwpr.www.instrumentlog;

import java.util.Date;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.chart.google.DataSet;
import org.uwpr.instrumentlog.*;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/** 
 * MyEclipse Struts
 * Creation date: 04-23-2008
 * 
 * XDoclet definition:
 * @struts.action
 * @struts.action-forward name="Success" path="/pages/admin/instrumentlog/allInstrumentUsageSummary.jsp"
 */
public class AllInstrumentUsageSummaryAction extends Action {
	/*
	 * Generated Methods
	 */

	private static final Logger log = LogManager.getLogger(AllInstrumentUsageSummaryAction.class);
	
	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}
		
		// Restrict access to administrators
        Groups groupMan = Groups.getInstance();
        if (!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
            ActionErrors errors = new ActionErrors();
            errors.add("access", new ActionMessage("error.access.invalidgroup"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
		
		// If this is the very first request for this action (when the user clicks the "MS LOG" tab
		// the values in the form will be uninitialized.
		DateRangeForm dateForm = (DateRangeForm)form;
		if (!dateForm.isInitialized())
			dateForm = null;
		
		// If not DateRangeForm was found in the request, we will return instrument usage summary
		// for 90 days
		Date startDate = DateUtils.currentMinusDays(59);
		Date endDate = DateUtils.currentPlusDays(30); // set time to the end of the day
		
		
		if (dateForm == null) {
			// create a new date range form
			dateForm = new DateRangeForm();
			dateForm.setStartDateDay(DateUtils.getDay(startDate));
			dateForm.setStartDateMonth(DateUtils.getMonth(startDate));
			dateForm.setStartDateYear(DateUtils.getYear(startDate));
			
			
			dateForm.setEndDateDay(DateUtils.getDay(endDate));
			dateForm.setEndDateMonth(DateUtils.getMonth(endDate));
			dateForm.setEndDateYear(DateUtils.getYear(endDate));
			// put / update the form in the session for future use.
			request.getSession().setAttribute("dateRangeForm", dateForm);
		}
		else {
			startDate = DateUtils.getDate(dateForm.getStartDateDay(),
								dateForm.getStartDateMonth(), 
								dateForm.getStartDateYear());
			endDate = DateUtils.getDate(dateForm.getEndDateDay(), 
								dateForm.getEndDateMonth(), 
								dateForm.getEndDateYear(), 
								true); // set the time to the end of the day
		}
		
		
		// if start date is greater than end date return an error
		if (startDate.compareTo(endDate) == 1) {
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.daterange"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		
		
		// get the usage stats
		List<MsInstrument> instruments = MsInstrumentUtils.instance().getMsInstruments();
		List<UsageBlock> usageBlks;
		try
		{
			// Trim blocks that go beyond the given start and end dates.
			UsageBlockFilter filter = new UsageBlockFilter();
			filter.setStartDate(startDate);
			filter.setEndDate(endDate);
			filter.setTrimToFit(true);
			usageBlks = UsageBlockDAO.getUsageBlocks(filter);
		}
		catch (SQLException e1)
		{
			log.error("Error loading usage blocks.", e1);
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.loaderror", e1.getMessage()));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		HttpSession session = request.getSession();
		
		DataSet instrStats = UsageStatsCalculator.calculateInstrumentStats(usageBlks, instruments, startDate, endDate);
		if (instrStats.hasData())
			session.setAttribute("allInstrumentStats", instrStats);
		else
			session.removeAttribute("allInstrumentStats");
		
		DataSet piStats = UsageStatsCalculator.calculatePIStats(usageBlks, startDate, endDate, instruments.size());
		if (piStats.hasData())
			session.setAttribute("allPiStats", piStats);
		else
			session.removeAttribute("allPiStats");
		
		DataSet projectStats = UsageStatsCalculator.calculateProjectStats(usageBlks, startDate, endDate);
		if(projectStats.hasData())
		    session.setAttribute("allProjectStats", projectStats);
		else
		    session.removeAttribute("allProjectStats");
		
		DataSet monthlyStats = UsageStatsCalculator.calculateMonthlyStats(usageBlks, instruments, startDate, endDate, false);
		if (monthlyStats.hasData())
			session.setAttribute("allMonthlyStats", monthlyStats);
		else
			session.removeAttribute("allMonthlyStats");
		
		// get the usage summary for all instruments (active and retired)
		List<MsInstrumentUsage> usageList = null;
		try {
			usageList = MsInstrumentUtils.instance().getAllInstrumentUsage(startDate, endDate);
		} catch (SQLException e) {
			log.error("Error loading instrument usage.", e);
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.loaderror", e.getMessage()));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		session.setAttribute("usageList", usageList);
		return mapping.findForward("Success");
	}
}