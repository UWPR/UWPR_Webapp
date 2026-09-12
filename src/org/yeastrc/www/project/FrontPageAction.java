/* FrontPageAction.java
 * Created on Jun 23, 2004
 */
package org.yeastrc.www.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.notice.NoticeDAO;
import org.yeastrc.project.Collaboration;
import org.yeastrc.project.Project;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;


/**
 * Controller class for viewing all highlights.
 */
public class FrontPageAction extends Action {

	private static final Logger log = LogManager.getLogger(FrontPageAction.class);

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

		// Get all NEW projects for this YRC member
		Collection c = user.getNewProjects();
		request.setAttribute("newProjects", c);

		// Split into the two lists the home page shows.  Set both even when empty -- front.jsp
		// forwards back here if activeProjects is missing.
		List<Project> activeProjects = new ArrayList<Project>();
		List<Project> archivedProjects = new ArrayList<Project>();

		for (Project project: user.getProjects()) {
			if (project.isArchived()) {
				archivedProjects.add(project);
			}
			else {
				activeProjects.add(project);
			}
		}

		request.setAttribute("activeProjects", activeProjects);
		request.setAttribute("archivedProjects", archivedProjects);

		Groups groupMan = Groups.getInstance();
		if (groupMan.isMember(user.getResearcher().getID(), "administrators")) {

		}

		// Active home-page notices. Don't let a query failure break the home page.
		try {
			request.setAttribute("notices", NoticeDAO.getInstance().getActiveNotices(new Date()));
		}
		catch (Exception e) {
			log.error("Failed to load active notices for the home page", e);
		}

		// Go!
		return mapping.findForward("Success");
	}	
}
