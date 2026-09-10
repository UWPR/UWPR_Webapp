/**
 * ArchiveProjectsAction.java
 */
package org.yeastrc.www.project;

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
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * Archives or unarchives projects.  Only changes how they are listed on the home page --
 * nothing is deleted, and billing, scheduling and the calendar are unaffected.
 *
 * Serves the details page link and the home page bulk buttons.  Parameters:
 *  - projectIds -- one or more project IDs
 *  - archived   -- "true" or "false".  An explicit direction, not a toggle, so a mixed
 *                  selection all ends up the same way
 *  - returnTo   -- optional project ID to return to instead of the home page
 */
public class ArchiveProjectsAction extends Action {

	private static final Logger log = LogManager.getLogger(ArchiveProjectsAction.class);

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

		String[] projectIds = request.getParameterValues("projectIds");

		// Nothing selected.  The button is disabled in this case, so just go back.
		if (projectIds == null || projectIds.length == 0) {
			return returnForward(mapping, request);
		}

		boolean archived = Boolean.parseBoolean(request.getParameter("archived"));

		boolean deniedAny = false;

		for (String projectIdStr: projectIds) {

			int projectId;
			try {
				projectId = Integer.parseInt(projectIdStr);
			} catch (NumberFormatException e) {
				// Ignore anything that is not a project ID and keep going with the rest.
				log.warn("Ignoring non-numeric projectIds value in archive request: " + projectIdStr);
				continue;
			}

			Project project;
			try {
				project = ProjectFactory.getProject(projectId);
			} catch (Exception e) {
				// Project is gone or of an unknown type.  Skip it rather than failing the batch.
				log.warn("Could not load project " + projectId + " while archiving", e);
				continue;
			}

			// The IDs come straight off the request, so this is the check that matters.
			// Hiding the link in the JSP is convenience only.
			if (!project.checkAccess(user.getResearcher())) {
				log.warn("Researcher " + user.getResearcher().getID()
						+ " tried to archive project " + projectId + " without access");
				deniedAny = true;
				continue;
			}

			if (project.isArchived() != archived) {
				project.setArchived(archived);
				project.save();
			}
		}

		if (deniedAny) {
			ActionErrors errors = new ActionErrors();
			errors.add("access", new ActionMessage("error.project.noaccess"));
			saveErrors( request, errors );
		}

		return returnForward(mapping, request);
	}

	/**
	 * Back to the project named by returnTo, or the home page if there isn't one.
	 */
	private ActionForward returnForward(ActionMapping mapping, HttpServletRequest request) {

		String returnTo = request.getParameter("returnTo");

		if (returnTo != null && !returnTo.equals("")) {
			try {
				int returnToId = Integer.parseInt(returnTo);
				ActionForward forward = new ActionForward();
				forward.setPath("/viewProject.do?ID=" + returnToId);
				forward.setRedirect(true);
				return forward;
			} catch (NumberFormatException e) {
				// Fall through to the home page.
				log.warn("Ignoring non-numeric returnTo value in archive request: " + returnTo);
			}
		}

		return mapping.findForward("Success");
	}
}
