/* DeleteProjectAction.java
 * Created on Mar 30, 2004
 */
package org.yeastrc.www.project;

import java.sql.SQLException;

import javax.servlet.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;

import org.uwpr.instrumentlog.InstrumentUsageDAO;
import org.yeastrc.project.*;
import org.yeastrc.www.user.*;

/**
 * Implements the logic to delete a project
 */
public class DeleteProjectAction extends Action {

	private static final Logger log = LogManager.getLogger(DeleteProjectAction.class);

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {
		// Get the projectID they're after
		int projectID;
		
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
			return mapping.findForward("standardHome");
		}


		try {
			String strID = request.getParameter("ID");

			if (strID == null || strID.equals("")) {
				ActionErrors errors = new ActionErrors();
				errors.add("username", new ActionMessage("error.project.noprojectid"));
				saveErrors( request, errors );
				return mapping.findForward("standardHome");
			}

			projectID = Integer.parseInt(strID);

		} catch (NumberFormatException nfe) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.project.invalidprojectid"));
			saveErrors( request, errors );
			return mapping.findForward("standardHome");
		}

		// Refuse to delete a project with instrument time scheduled.  The leftover instrumentUsage
		// rows would break the monthly billing export and the instrument calendar.
		try {
			if (InstrumentUsageDAO.getInstance().getScheduledUsageBlockCountForProject(projectID) > 0) {
				ActionErrors errors = new ActionErrors();
				errors.add("project", new ActionMessage("error.project.hasinstrumenttime"));
				saveErrors( request, errors );
				return mapping.findForward("standardHome");
			}
		} catch (SQLException e) {
			// The project exists.  Only the scheduled-time check failed.
			log.error("Error checking scheduled instrument time for project " + projectID, e);
			ActionErrors errors = new ActionErrors();
			errors.add("project", new ActionMessage("error.project.instrumenttimecheckfailed"));
			saveErrors( request, errors );
			return mapping.findForward("standardHome");
		}

		// Load our project
		Project project;

		try {
			project = ProjectFactory.getProject(projectID);
			project.delete();
		} catch (Exception e) {
			
			// Couldn't load the project.
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.project.projectnotfound"));
			saveErrors( request, errors );
			return mapping.findForward("standardHome");	
		}

		return mapping.findForward("Success");

	}
	
}