/* SearchProjectsAction.java
 * Created on Apr 6, 2004
 */
package org.yeastrc.www.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.Collaboration;
import org.yeastrc.project.CollaborationAndReview;
import org.yeastrc.project.CollaborationStatus;
import org.yeastrc.project.BilledProject;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectReviewer;
import org.yeastrc.project.ProjectReviewerDAO;
import org.yeastrc.project.ProjectsSearcher;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;
/**
 * Add one sentence class summary here.
 * Add class description here
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, Apr 6, 2004
 *
 */
public class SearchProjectsAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {
		String searchString;
		String[] groups;
		String[] types;
		String[] statusTypes;
		
		HttpSession session = request.getSession();
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}

		// The Researcher
		Researcher researcher = user.getResearcher();

		// Get our search parameters
		searchString = ((SearchProjectsForm)(form)).getSearchString();
		groups = ((SearchProjectsForm)(form)).getGroups();
		types = ((SearchProjectsForm)(form)).getTypes();
		statusTypes = ((SearchProjectsForm)(form)).getCollaborationStatus();
		
		// Did the user click on the "Show All" button? If so, we will 
		// ignore search constraints and list all projects.
		if(((SearchProjectsForm)(form)).getShowAll() != null) {
		    searchString = "";
		    groups = null;
		    types = null;
		    statusTypes = null;
		}
		
		
		// Get our project search object
		ProjectsSearcher ps = new ProjectsSearcher();
		
		// Add our search tokens
		if (searchString != null && !searchString.equals("")) {
			StringTokenizer st = new StringTokenizer(searchString);			
			while (st.hasMoreTokens()) {
					 ps.addSearchToken(st.nextToken());
			}
		}

		// Add our groups
		if (groups != null) {
			for (int i = 0; i < groups.length; i++) {
				ps.addGroup(groups[i]);
			}
		}
		
		// Add our types
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				ps.addType(types[i]);
			}
		}
		
		// Add our status types
		if(statusTypes != null) {
		    for(String statusStr: statusTypes) {
		        CollaborationStatus status = CollaborationStatus.statusForChar(statusStr.charAt(0));
		        ps.addStatusType(status);
		        if(status == CollaborationStatus.PENDING)
		        	ps.addStatusType(CollaborationStatus.REVISE_PENDING);
		    }
		}

		// Put the access constraint on the search
		ps.setResearcher(researcher);
		
		// Get our list of projects
		List<Project> projects = ps.search();
		// sort in descending order of project IDs
		Collections.sort(projects, new Comparator<Project>() {
			public int compare(Project o1, Project o2) {
				return Integer.valueOf(o2.getID()).compareTo(o1.getID());
			}
		});
		
		// separate into billed projects and subsidized projects
		List<BilledProject> billedProjects = new ArrayList<BilledProject>();
		session.setAttribute("billedProjects", billedProjects);
		for(Project project: projects) {
			if(project instanceof BilledProject)
				billedProjects.add((BilledProject) project);
		}
		
		
		
		List<CollaborationAndReview> projAndRev = new ArrayList<CollaborationAndReview>();
		session.setAttribute("subsidizedProjects", projAndRev);
		// Get the reviewers for the projects
		ProjectReviewerDAO prDao = ProjectReviewerDAO.instance();
		for(Project project: projects) {
			if(!(project instanceof Collaboration))
				continue;
		    CollaborationAndReview cnr = new CollaborationAndReview();
		    cnr.setCollaboration((Collaboration)project);
		    List<ProjectReviewer> reviewers = prDao.getProjectReviewers(((Collaboration)project).getID());
		    cnr.setReviewers(reviewers);
		    projAndRev.add(cnr);
		}
		
		
		session.setAttribute("projectsSearchSize", new Integer(projects.size()));

		// Go!
		return mapping.findForward("Success");

	}
}
