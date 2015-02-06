/*
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project;

import org.apache.struts.action.*;
import org.yeastrc.project.ExemptResearchers;
import org.yeastrc.project.Project;
import org.yeastrc.project.Projects;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Controller class for creating a new Collaboration.
 */
public class NewCollaborationAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {
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
		
		// Check if this researcher has any projects with overdue reports
		if(!ExemptResearchers.contains(researcher.getID())) {
		    List<Project> overdueProjects = user.getProjectsWithOverdueReports();
		    if(overdueProjects.size() > 0) {
		        request.setAttribute("overdueProjects", overdueProjects);
		        return mapping.findForward("BlockCollaboration");
		    }
		}

		
		// Create our ActionForm
		EditCollaborationForm newForm = new EditCollaborationForm();
		request.setAttribute("editCollaborationForm", newForm);
		
		// Set the default researcher to this user
        List<Researcher> researcherIds = new ArrayList<Researcher>();
        researcherIds.add(researcher);
        newForm.setResearcherList(researcherIds);

		// Set up a Collection of all the Researchers to use in the form as a pull-down menu for researchers
		Collection researchers = Projects.getAllResearchers();
		session.setAttribute("researchers", researchers);
		
		List<Researcher> labDirectors = Projects.getAllLabDirectors();
		session.setAttribute("labDirectors", labDirectors);
		
		// Go!
		return mapping.findForward("Success");

	}
	
}