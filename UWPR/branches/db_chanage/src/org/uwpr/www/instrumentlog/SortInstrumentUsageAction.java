package org.uwpr.www.instrumentlog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.instrumentlog.MsInstrumentUsage;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

public class SortInstrumentUsageAction extends Action {

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
		
		HttpSession session = request.getSession();
		MsInstrumentUsage usage = (MsInstrumentUsage) session.getAttribute("usage");
		
		
		// if usage was not found in the session, return error
		if (usage == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.sort.nodatafound"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		// What is the sorting criterion
		String sortby = request.getParameter("sortby");
		if (sortby == null || sortby.equals("")) {
			return mapping.findForward("Failure");
		}
		
		if (sortby.equalsIgnoreCase("ProjectID"))
			usage.sortByProjectID();
		else if (sortby.equalsIgnoreCase("Title"))
			usage.sortByProjectTitle();
		else if (sortby.equalsIgnoreCase("PI"))
			usage.sortByPI();
		else if (sortby.equalsIgnoreCase("Usage"))
			usage.sortByUsage();
		else
			return mapping.findForward("Failure");
		
		return mapping.findForward("Success");
	}
	
}
