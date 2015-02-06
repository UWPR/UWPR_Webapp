package org.yeastrc.www.project;

import org.apache.log4j.Logger;
import org.apache.struts.action.*;
import org.yeastrc.project.*;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.List;

/**
 * Controller class for creating a new Billed Project
 */
public class ExtendAsBilledProjectAction extends Action {

	private static final Logger log = Logger.getLogger(ExtendAsBilledProjectAction.class);
	
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

		// we need a parent projectID
		int projectId = 0;
		String intStr = request.getParameter("projectId");
		if(intStr == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.project.noprojectid"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
			
		try {
			projectId = Integer.parseInt(intStr);
		}
		catch(NumberFormatException e) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.project.invalidprojectid"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		if(projectId == 0) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.project.invalidprojectid"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		// load the parent project and make sure the user has access
        Project project = null;
        try {
        	project = ProjectFactory.getProject(projectId);
        	if(!project.checkAccess(user.getResearcher())) {
        		// This user doesn't have access to this project.
                ActionErrors errors = new ActionErrors();
                errors.add("username", new ActionMessage("error.project.noaccess"));
                saveErrors( request, errors );
        		return mapping.findForward("Failure");
        	}
        }
        catch(Exception e) {
        	ActionErrors errors = new ActionErrors();
            errors.add("project", new ActionMessage("error.project.projectnotfound"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");  
        }
        
		// Create our ActionForm
		EditBilledProjectForm editBilledProjectForm = (EditBilledProjectForm) form;
		editBilledProjectForm.setParentProjectID(projectId);
		editBilledProjectForm.setTitle(project.getTitle());
		editBilledProjectForm.setScientificQuestion(project.getScientificQuestion());
		editBilledProjectForm.setAbstract(project.getAbstract());
		editBilledProjectForm.setGroups(project.getGroupsArray());
		editBilledProjectForm.setComments(project.getComments());
		editBilledProjectForm.setPublications(project.getPublications());
		
		// Set the Researchers
        Researcher res = project.getPI();
        if (res != null) editBilledProjectForm.setPI(res.getID());
        
        editBilledProjectForm.setResearcherList(project.getResearchers());
        
		request.setAttribute("editBilledProjectForm", editBilledProjectForm);
		
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