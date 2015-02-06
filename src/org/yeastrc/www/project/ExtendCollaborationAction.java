/**
 * ExtendCollaborationAction.java
 * @author Vagisha Sharma
 * Apr 1, 2009
 * @version 1.0
 */
package org.yeastrc.www.project;

import org.apache.struts.action.*;
import org.yeastrc.project.*;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 
 */
public class ExtendCollaborationAction extends Action {

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

        // Get the projectID they're after
        int projectID;
        try {
            String strID = request.getParameter("ID");

            if (strID == null || strID.equals("")) {
                ActionErrors errors = new ActionErrors();
                errors.add("username", new ActionMessage("error.project.noprojectid"));
                saveErrors( request, errors );
                return mapping.findForward("Failure");
            }

            projectID = Integer.parseInt(strID);

        } catch (NumberFormatException nfe) {
            ActionErrors errors = new ActionErrors();
            errors.add("username", new ActionMessage("error.project.invalidprojectid"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        // Load our project
        Project project;
        
        try {
            project = ProjectFactory.getProject(projectID);
            if (!project.checkAccess(user.getResearcher())) {
                
                // This user doesn't have access to this project.
                ActionErrors errors = new ActionErrors();
                errors.add("username", new ActionMessage("error.project.noaccess"));
                saveErrors( request, errors );
                return mapping.findForward("Failure");
            }
        } 
        catch (Exception e) {
            
            // Couldn't load the project.
            ActionErrors errors = new ActionErrors();
            errors.add("project", new ActionMessage("error.project.projectnotfound"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");  
        }

        

        // Create our ActionForm
        EditCollaborationForm newForm = new EditCollaborationForm();
        request.setAttribute("editCollaborationForm", newForm);

        
        // Set the parent project
        newForm.setParentProjectID(projectID);
        // This is a new project it should be editable
        ((EditCollaborationForm)(newForm)).setFullEditable(true);
        
        String[] groups = project.getGroupsArray();
        ((EditCollaborationForm)(newForm)).setGroups(groups);
        
        Date dateAccepted = ((Collaboration)project).getDateAccepted();
        if(dateAccepted != null)
            ((EditCollaborationForm)(newForm)).setDateAccepted(dateAccepted.toString());
        
        
        newForm.setSubmitDate(project.getSubmitDate().toString());

        newForm.setFundingTypes(project.getFundingTypesArray());
        newForm.setFederalFundingTypes(project.getFederalFundingTypesArray());
        newForm.setGrantAmount( project.getGrantAmount() );
        newForm.setGrantNumber( project.getGrantNumber() );
        newForm.setFoundationName( project.getFoundationName() );
        
        newForm.setDatabaseSearchRequested( project.isDatabaseSearchRequested() );
        newForm.setMassSpecExpertiseRequested( project.isMassSpecExpertiseRequested() );
        newForm.setLtqRunsRequested( project.getLtqRunsRequested() );
        newForm.setLtq_etdRunsRequested( project.getLtq_etdRunsRequested());
        newForm.setLtq_orbitrapRunsRequested( project.getLtq_orbitrapRunsRequested() );
        newForm.setLtq_ftRunsRequested( project.getLtq_ftRunsRequested() );
        newForm.setTsq_accessRunsRequested( project.getTsq_accessRunsRequested() );
        newForm.setTsq_vantageRunsRequested( project.getTsq_vantageRunsRequested());
        newForm.setFragmentationTypes( project.getFragmentationTypesArray() );
        newForm.setTitle(project.getTitle());
        newForm.setAbstract(project.getAbstract());
        newForm.setScientificQuestion( project.getScientificQuestion() );
        newForm.setPublicAbstract(project.getPublicAbstract());
        newForm.setProgress(project.getProgress());
        //newForm.setKeywords(project.getKeywords());
        newForm.setComments(project.getComments());
        newForm.setPublications(project.getPublications());
        
        // Set the admin parameters
        newForm.setBTA(project.getBTA());
        //newForm.setAxisI(project.getAxisI());
        //newForm.setAxisII(project.getAxisII());

        // Set the Researchers
        Researcher res = project.getPI();
        if (res != null) newForm.setPI(res.getID());
        
        newForm.setResearcherList(project.getResearchers());
        

        // Set up a Collection of all the Researchers to use in the form as a pull-down menu for researchers
        Collection researchers = Projects.getAllResearchers();
        session.setAttribute("researchers", researchers);
        
        List<Researcher> labDirectors = Projects.getAllLabDirectors();
        // If this is an old project, the user listed as lab director may not be in the "Lab Directors" group
        Researcher pi = project.getPI();
        boolean found = false;
        for(Researcher ld: labDirectors) {
        	if(pi.getID() == ld.getID()) {
        		found = true;
        		break;
        	}
        }
        if(!found)
        	labDirectors.add(pi);
		session.setAttribute("labDirectors", labDirectors);

        // Go!
        return mapping.findForward("Success");

    }
}
