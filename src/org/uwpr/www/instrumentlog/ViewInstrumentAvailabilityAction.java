package org.uwpr.www.instrumentlog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.instrumentlog.DateUtils;
import org.uwpr.instrumentlog.InstrumentCalendar;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.MsInstrumentUtils;
import org.yeastrc.www.taglib.InstrumentCalendarTag;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

public class ViewInstrumentAvailabilityAction extends Action {

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws Exception 
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}
		
		int instrumentID = 0;
		
		// look for the instrumentID in the form
		InstrumentSelectorForm instrSelForm = (InstrumentSelectorForm)form;
		if (instrSelForm.getInstrumentID() != null) {
			instrumentID = Integer.parseInt(instrSelForm.getInstrumentID());
		}
		
		// otherwise look for the instrument ID in the request parameter.
		else {
			try {
				instrumentID = Integer.parseInt(request.getParameter("instrumentID"));
			}
			catch (NumberFormatException e) {
				instrumentID = 0;
			}
		}
		
		if (instrumentID == 0){
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.instrumentid"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		// if the form in the request is not valid put a valid form in the request
		if (!instrSelForm.isFormInitialized()) {
			instrSelForm = new InstrumentSelectorForm();
			request.setAttribute("selectorForm", instrSelForm);
		}
		instrSelForm.setInstrumentID(instrumentID+"");
		
		
		// get a list of ms instruments and put them in the request
		List <MsInstrument> instruments = MsInstrumentUtils.instance().getMsInstruments();
		Collections.sort(instruments, new Comparator<MsInstrument>() {
			public int compare(MsInstrument o1, MsInstrument o2) {
				return o1.getID() > o2.getID() ? 1 : (o1.getID() == o2.getID() ? 0 : -1);
			}});
		
		request.setAttribute("instruments", instruments);
		
		int month = DateUtils.getCurrentMonth();
		int year = DateUtils.getCurrentYear();
		
		if (request.getParameter("month") != null)
			month = Integer.parseInt(request.getParameter("month"));
		
		if (request.getParameter("year") != null) 
			year = Integer.parseInt(request.getParameter("year"));
			
		InstrumentCalendar calendar = MsInstrumentUtils.instance().getInstrumentCalendar(month, year, instrumentID);
		request.setAttribute(InstrumentCalendarTag.CAL_BEAN_NAME, calendar);
		
		return mapping.findForward("Success");
	}
	
}
