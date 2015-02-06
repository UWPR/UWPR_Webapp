/**
 * ToggleBilledProjectStatus.java
 * @author Vagisha Sharma
 * Jun 15, 2011
 */
package org.yeastrc.www.project;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.BilledProject;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * 
 */
public class ToggleBilledProjectStatus extends Action{

	//private static final Logger log = Logger.getLogger(ToggleBilledProjectStatus.class);
    
    public ActionForward execute( ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response )
    throws Exception {
        

        // User making this request
        User user = UserUtils.getUser(request);
        if (user == null) {
            ActionErrors errors = new ActionErrors();
            errors.add("username", new ActionMessage("error.login.notloggedin"));
            saveErrors( request, errors );
            return mapping.findForward("authenticate");
        }

        // Make sure user is either a administrator
        Groups groupMan = Groups.getInstance();
        if (!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
            ActionErrors errors = new ActionErrors();
            errors.add("access", new ActionMessage("error.access.invalidgroup"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }

        
        int projectId = 0;
        try {
			String strID = request.getParameter("projectId");

			if (strID == null || strID.equals("")) {
				ActionErrors errors = new ActionErrors();
				errors.add("username", new ActionMessage("error.project.noprojectid"));
				saveErrors( request, errors );
				return mapping.findForward("Failure");
			}

			projectId = Integer.parseInt(strID);

		} catch (NumberFormatException nfe) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.project.invalidprojectid"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}

		// Load our project
		BilledProject project;
		
		try {
			project = (BilledProject)(ProjectFactory.getProject(projectId));
			if (!project.checkAccess(user.getResearcher())) {
				
				// This user doesn't have access to this project.
				ActionErrors errors = new ActionErrors();
				errors.add("username", new ActionMessage("error.project.noaccess"));
				saveErrors( request, errors );
				return mapping.findForward("Failure");
			}
		} catch (Exception e) {
			
			// Couldn't load the project.
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.project.projectnotfound"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");	
		}
        
		if(project.isBlocked()) {
			project.setBlocked(false);
		}
		else
			project.setBlocked(true);
		
        
        // save
        project.save();
        
        
        // Go!
        ActionForward forward = new ActionForward();
        forward.setPath(mapping.findForward("Success").getPath()+"?ID="+projectId);
        forward.setRedirect(true);
        return forward;
    }
}
