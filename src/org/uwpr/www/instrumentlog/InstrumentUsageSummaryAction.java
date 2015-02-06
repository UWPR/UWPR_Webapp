package org.uwpr.www.instrumentlog;

import java.util.Date;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.chart.google.DataSet;
import org.uwpr.instrumentlog.DateUtils;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.MsInstrumentUsage;
import org.uwpr.instrumentlog.MsInstrumentUtils;
import org.uwpr.instrumentlog.UsageBlock;
import org.uwpr.instrumentlog.UsageStatsCalculator;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

public class InstrumentUsageSummaryAction extends Action {

	private static final Logger log = Logger.getLogger(InstrumentUsageSummaryAction.class);
	
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
        
		DateRangeForm dateForm = (DateRangeForm)form;
		
		// If not DateRangeForm was found in the session, we will return instrument usage summary
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
		
		// put / update the form in the session for future use.
		request.getSession().setAttribute("dateRangeForm", dateForm);
		
		// if start date is greater than end date return an error
		if (startDate.compareTo(endDate) == 1) {
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.daterange"));
			saveErrors( request, errors );
		}
		
		int instrumentID = 0;
		
		// look for instrumentID in the request parameters first
		if (request.getParameter("instrumentID") != null) {
			instrumentID = Integer.parseInt(request.getParameter("instrumentID"));
		}
		// look for it in the form
		else {
			instrumentID = dateForm.getInstrumentID();
		}
		
		if (instrumentID == 0) {
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.instrumentid"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		HttpSession session = request.getSession();
		
		// get the usage stats
		List<UsageBlock> usageBlks;
		try {
			usageBlks = MsInstrumentUtils.instance().getUsageBlocksForInstrument(instrumentID, startDate, endDate);
		} catch (SQLException e1) {
			log.error("Error loading instrument usage.", e1);
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.loaderror", e1.getMessage()));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		
		DataSet piStats = UsageStatsCalculator.calculatePIStats(usageBlks, startDate, endDate, 1); // only looking at stats for one instrument
		if (piStats.hasData())
			session.setAttribute("piStats", piStats);
		else
			session.removeAttribute("piStats");
		
		List<MsInstrument> instruments = MsInstrumentUtils.instance().getMsInstruments();
		DataSet monthlyStats = UsageStatsCalculator.calculateMonthlyStats(usageBlks, instruments, startDate, endDate, false);
		if (monthlyStats.hasData())
			session.setAttribute("monthlyStats", monthlyStats);
		else
			session.removeAttribute("monthlyStats");
		
		// get usage details for this instrument
		MsInstrumentUsage usage = null;
		try {
			usage = MsInstrumentUtils.instance().getInstrumentUsage(instrumentID, startDate, endDate);
		} catch (SQLException e) {
			log.error("Error loading instrument usage.", e);
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.loaderror", e.getMessage()));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		session.setAttribute("usage", usage);
		
		return mapping.findForward("Success");
	}
}
