package org.yeastrc.www.project;

import org.apache.struts.action.*;
import org.yeastrc.project.Affiliation;
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
 * Controller class for creating a new Billed Project
 */
public class NewBilledProjectAction extends Action {

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

		
		// Create our ActionForm
		EditBilledProjectForm editBilledProjectForm = new EditBilledProjectForm();
		request.setAttribute("editBilledProjectForm", editBilledProjectForm);
		
		// Set the default researcher to this user
        List<Researcher> researcherIds = new ArrayList<Researcher>();
        researcherIds.add(researcher);
        editBilledProjectForm.setResearcherList(researcherIds);


		// Set up a Collection of all the Researchers to use in the form as a pull-down menu for researchers
		Collection researchers = Projects.getAllResearchers();
		session.setAttribute("researchers", researchers);
		
		List<Researcher> labDirectors = Projects.getAllLabDirectors();
		session.setAttribute("labDirectors", labDirectors);
		
		List<Affiliation> affiliationTypes = Affiliation.getList();
		session.setAttribute("affiliationTypes", affiliationTypes);
		
		// Go!
		return mapping.findForward("Success");

	}
	
}