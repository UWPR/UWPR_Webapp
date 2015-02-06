package org.uwpr.www.instrumentlog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.instrumentlog.InstrumentUsageDAO;
import org.uwpr.instrumentlog.MsInstrumentUtils;
import org.uwpr.instrumentlog.UsageBlockBase;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

public class DeleteInstrumentUsageAction extends Action {

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
		
		// Restrict access to administrators
        Groups groupMan = Groups.getInstance();
        if (!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
            ActionErrors errors = new ActionErrors();
            errors.add("access", new ActionMessage("error.access.invalidgroup"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
		
		int usageID = 0;
		try {
			usageID = Integer.parseInt((String)request.getParameter("usageID"));
		}
		catch (NumberFormatException e) {
			usageID = 0;
		}
		
		if (usageID == 0) {
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.usageid"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		UsageBlockBase blk = MsInstrumentUtils.instance().getUsageBlockBase(usageID);
		if (blk == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.usageid"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		InstrumentUsageDAO.getInstance().delete(usageID);
		
		int instrumentID = 0;
		try {
			instrumentID = Integer.parseInt((String)request.getParameter("instrumentID"));
		}
		catch (NumberFormatException e) {
			instrumentID = 0;
		}
		if (instrumentID == 0) {
			ActionErrors errors = new ActionErrors();
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.instrumentid"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		ActionForward af = mapping.findForward( "Success" ) ;
		af = new ActionForward( af.getPath() + "?instrumentID=" + instrumentID, af.getRedirect() ) ;
		return af;

	}
}
