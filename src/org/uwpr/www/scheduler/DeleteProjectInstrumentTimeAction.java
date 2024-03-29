/**
 * DeleteProjectInstrumentTimeAjaxAction.java
 * @author Vagisha Sharma
 * May 28, 2011
 */
package org.uwpr.www.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;
import org.uwpr.instrumentlog.*;
import org.uwpr.scheduler.UsageBlockDeletableDecider;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

/**
 * 
 */
public class DeleteProjectInstrumentTimeAction extends Action {

	private static final Logger log = LogManager.getLogger(DeleteProjectInstrumentTimeAction.class);
	
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
        int projectId = 0;
        try {
        	projectId = Integer.parseInt(request.getParameter("projectId"));
        }
        catch(NumberFormatException e) {
        	projectId = 0;
        }
        if(projectId == 0) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.scheduler.invalidid", 
            		"Invalid projectId: "+request.getParameter("projectId")+" in request"));
            saveErrors( request, errors );
            return mapping.findForward("standardHome");
        }
        
        if(projectId == 0) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.scheduler.invalidid", "Invalid projectID in request"));
            saveErrors( request, errors );
            return mapping.findForward("standardHome");
        }
        
        // Make sure the user has access to the project
        Project project = null;
        try {
        	project = ProjectFactory.getProject(projectId);
        	if(!project.checkAccess(user.getResearcher())) {
        		ActionErrors errors = new ActionErrors();
        		errors.add("scheduler", new ActionMessage("error.scheduler.invalidaccess",
        				"User does not have access to delete instrument time for project."));
        		saveErrors( request, errors );
        		ActionForward fwd = mapping.findForward("Failure");
    			ActionForward newFwd = new ActionForward(fwd.getPath()+"?projectId="+projectId, fwd.getRedirect());
            	return newFwd;
        	}
        }
        catch(Exception e) {
        	ActionErrors errors = new ActionErrors();
			errors.add("scheduler", new ActionMessage("error.scheduler.load","Error loading project to check access."));
			saveErrors( request, errors );
			log.error("Error checking access to project ID: "+projectId, e);
			ActionForward fwd = mapping.findForward("Failure");
			ActionForward newFwd = new ActionForward(fwd.getPath()+"?projectId="+projectId, fwd.getRedirect());
        	return newFwd;
        }
        
        // get the usage block ID
        int usageBlockId = 0;
        try {
        	usageBlockId = Integer.parseInt(request.getParameter("usageBlockId"));
        }
        catch(NumberFormatException e) {
        	usageBlockId = 0;
        }
        if(usageBlockId == 0) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.scheduler.invalidid", 
            		"Invalid usageBlockId: "+request.getParameter("usageBlockId")+" in request"));
            saveErrors( request, errors );
            ActionForward fwd = mapping.findForward("Failure");
			ActionForward newFwd = new ActionForward(fwd.getPath()+"?projectId="+projectId, fwd.getRedirect());
        	return newFwd;
        }
        
        UsageBlockBase usageBlock = UsageBlockBaseDAO.getUsageBlockBase(usageBlockId);
        if(usageBlock == null) {
        	ActionErrors errors = new ActionErrors();
			errors.add("scheduler", new ActionMessage("error.scheduler.load","No usage block found for usageBlockId: "+usageBlockId));
			saveErrors( request, errors );
			ActionForward fwd = mapping.findForward("Failure");
			ActionForward newFwd = new ActionForward(fwd.getPath()+"?projectId="+projectId, fwd.getRedirect());
        	return newFwd;
        }
        
        StringBuilder errorMessage = new StringBuilder();
        
        if(!UsageBlockDeletableDecider.getInstance().isBlockDeletable(usageBlock, user, errorMessage)) {
        	ActionErrors errors = new ActionErrors();
    		errors.add("scheduler", new ActionMessage("error.scheduler.invalidaccess",
    				errorMessage.toString()));
    		saveErrors( request, errors );
    		ActionForward fwd = mapping.findForward("Failure");
			ActionForward newFwd = new ActionForward(fwd.getPath()+"?projectId="+projectId, fwd.getRedirect());
        	return newFwd;
        }


		boolean emailProjectResearchers = true;
		if (Groups.getInstance().isAdministrator(user.getResearcher()))
		{
			emailProjectResearchers = false;
		}

		log.info("Deleting usage block: Researcher: " + user.getIdAndName() + "; " + usageBlock.toString());
		InstrumentUsageDAO.getInstance().purge(usageBlock, user.getResearcher());

		// Email admins
		ProjectInstrumentUsageUpdateEmailer.Action action = ProjectInstrumentUsageUpdateEmailer.Action.DELETED;
		MsInstrument instrument = MsInstrumentUtils.instance().getMsInstrument(usageBlock.getInstrumentID());
		ProjectInstrumentUsageUpdateEmailer.getInstance().sendEmail(project, instrument, user.getResearcher(),
				Collections.singletonList(usageBlock), null,
				action, null, emailProjectResearchers);

		ActionForward fwd = mapping.findForward("Success");
		ActionForward newFwd = new ActionForward(fwd.getPath()+"?projectId="+projectId, fwd.getRedirect());
    	return newFwd;

	}
}
