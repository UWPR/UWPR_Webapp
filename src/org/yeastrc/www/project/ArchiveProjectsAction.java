/**
 * ArchiveProjectsAction.java
 */
package org.yeastrc.www.project;

import java.sql.SQLException;

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
import org.uwpr.instrumentlog.InstrumentUsageDAO;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectDAO;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * Archives or unarchives projects.  Nothing is deleted and billing is unaffected, but an
 * archived project takes no new payment methods and its instrument time cannot be changed.
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
			return returnForward(mapping, request, true);
		}

		// Boolean.parseBoolean would read a missing or misspelled parameter as unarchive.
		String direction = request.getParameter("archived");
		direction = direction == null ? null : direction.trim();

		if (!"true".equalsIgnoreCase(direction) && !"false".equalsIgnoreCase(direction)) {
			log.warn("Refusing archive request with archived=" + direction);
			ActionErrors errors = new ActionErrors();
			errors.add("archive", new ActionMessage("error.project.archivenodirection"));
			saveErrors( request, errors );
			return returnForward(mapping, request, false);
		}

		boolean archived = "true".equalsIgnoreCase(direction);

		boolean deniedAny = false;
		boolean failedAny = false;
		boolean hasFutureTimeAny = false;

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
				log.warn("User " + user.getID()
						+ " tried to archive project " + projectId + " without access");
				deniedAny = true;
				continue;
			}

			// An archived project's instrument time cannot be changed, so a project with time
			// still to come is not archived.
			if (archived && !project.isArchived()) {
				try {
					if (InstrumentUsageDAO.getInstance().getFutureUsageBlockCountForProject(projectId) > 0) {
						hasFutureTimeAny = true;
						continue;
					}
				} catch (SQLException e) {
					log.error("Could not check future instrument time for project " + projectId, e);
					failedAny = true;
					continue;
				}
			}

			if (project.isArchived() != archived) {
				// A targeted UPDATE, not project.save().  See ProjectDAO.setArchived.
				// Each project is separate, so one failure does not abandon the rest of
				// the batch part way through with no record of what was applied.
				try {
					ProjectDAO.instance().setArchived(projectId, archived);
				} catch (SQLException e) {
					log.error("Could not set archived=" + archived + " on project " + projectId, e);
					failedAny = true;
				}
			}
		}

		boolean anythingToReport = deniedAny || failedAny || hasFutureTimeAny;

		if (anythingToReport) {
			// The project page archives one project, the home page buttons archive a selection.
			boolean single = projectIds.length == 1;

			ActionErrors errors = new ActionErrors();
			if (deniedAny) {
				errors.add("access", new ActionMessage("error.project.noaccess"));
			}
			if (hasFutureTimeAny) {
				errors.add("archive", new ActionMessage("error.project.archivehasfuturetime"));
			}
			if (failedAny) {
				errors.add("archive", new ActionMessage(single ? "error.project.archivefailed.one"
						: "error.project.archivefailed.some"));
			}
			saveErrors( request, errors );
		}

		return returnForward(mapping, request, !anythingToReport);
	}

	/**
	 * Back to the project named by returnTo, or the home page if there isn't one.
	 *
	 * Struts 1.1 has no session-scoped saveErrors, so messages live in request scope and a
	 * redirect discards them.  Callers with something to report pass redirect false, which
	 * forwards to the same action instead and keeps the messages for errors.jsp.
	 */
	private ActionForward returnForward(ActionMapping mapping, HttpServletRequest request, boolean redirect) {

		String returnTo = request.getParameter("returnTo");

		if (returnTo != null && !returnTo.equals("")) {
			try {
				int returnToId = Integer.parseInt(returnTo);
				ActionForward forward = new ActionForward();
				forward.setPath("/viewProject.do?ID=" + returnToId);
				forward.setRedirect(redirect);
				return forward;
			} catch (NumberFormatException e) {
				// Fall through to the home page.
				log.warn("Ignoring non-numeric returnTo value in archive request: " + returnTo);
			}
		}

		ActionForward success = mapping.findForward("Success");
		if (redirect) {
			return success;
		}

		ActionForward forward = new ActionForward();
		forward.setPath(success.getPath());
		forward.setRedirect(false);
		return forward;
	}
}
