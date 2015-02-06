/*
 *
 * Created on February 5, 2004
 *
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project;

import org.apache.struts.action.*;
import org.yeastrc.project.*;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Controller class for editing a project.
 */
public class EditProjectAction extends Action {

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


		
		// Where do you want to go from here?
		String forwardStr = "";


		EditProjectForm newForm = null;

		// Forward them on to the happy success view page!		
		if (project.getShortType().equals(Projects.COLLABORATION)) {
			newForm = new EditCollaborationForm();			
			request.setAttribute("editCollaborationForm", newForm);
			forwardStr = "Collaboration";
			
			if (project instanceof Collaboration)  {// this should always be true
			    
			    Date dateAccepted = ((Collaboration)project).getDateAccepted();
                if(dateAccepted != null)
                    ((EditCollaborationForm)(newForm)).setDateAccepted(dateAccepted.toString());
			    
                // determine if the user viewing the form will be able to edit certain fields
                int editable = 0;
                if(Groups.getInstance().isMember(user.getID(), "administrators")) {
		            editable = ProjectEditableChecker.FULL_EDITABLE; // form is always editable if an admin is looking at it. 
		        }
                else {
                	editable = ProjectEditableChecker.getEditStatus((Collaboration) project);
                }
                ((EditCollaborationForm)(newForm)).setFullEditable(editable == ProjectEditableChecker.FULL_EDITABLE);
            	((EditCollaborationForm)(newForm)).setPartEditable(editable == ProjectEditableChecker.FULL_EDITABLE ||
        													       editable == ProjectEditableChecker.PART_EDITABLE);
            	
            	if(!((Collaboration)project).isPending()) {
            		((EditCollaborationForm)(newForm)).setNotPending(true);
            	}
			}
			
			if(((Collaboration)project).getCollaborationStatus() == CollaborationStatus.REVISE) {
			    request.setAttribute("statusIsRevise", true);
			}
			
			String[] groups = project.getGroupsArray();
			((EditCollaborationForm)(newForm)).setGroups(groups);
			
			// !!!!!!!!!!!!!!!!!!!!! PROJECT EXTENSION REASON !!!!!!!!!!!!!!!!!!!!!!!
			newForm.setExtensionReasons(project.getExtensionReasons());
		}

		else if (project.getShortType().equals(Projects.BILLED_PROJECT)) {
			newForm = new EditBilledProjectForm();			
			request.setAttribute("editBilledProjectForm", newForm);
			forwardStr = "BilledProject";
			
			String[] groups = project.getGroupsArray();
			((EditBilledProjectForm)(newForm)).setGroups(groups);
			((EditBilledProjectForm)(newForm)).setAffiliation(((BilledProject)project).getAffiliation());
		
			int editable = 0;
            if(Groups.getInstance().isMember(user.getID(), "administrators")) {
	            editable = ProjectEditableChecker.FULL_EDITABLE; // form is always editable if an admin is looking at it. 
	        }
            else {
            	editable = ProjectEditableChecker.getEditStatus((BilledProject) project);
            }
            
            ((EditBilledProjectForm)(newForm)).setEditable(editable == ProjectEditableChecker.FULL_EDITABLE);
			
			List<Affiliation> affiliationTypes = Affiliation.getList();
			request.getSession().setAttribute("affiliationTypes", affiliationTypes);
		}
		
		else {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.project.invalidtype"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}


		// Set the parameters available to all project types
		newForm.setID(project.getID());
		newForm.setTitle(project.getTitle());
		newForm.setAbstract(project.getAbstract());
		newForm.setScientificQuestion( project.getScientificQuestion() );
		newForm.setPublicAbstract(project.getPublicAbstract());
		newForm.setProgress(project.getProgress());
		newForm.setComments(project.getComments());
		newForm.setPublications(project.getPublications());
		
		
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
		newForm.setInstrumentTimeExpl(project.getInstrumentTimeExpl());
		
		// Set the Researchers
		Researcher res = project.getPI();
		if (res != null) newForm.setPI(res.getID());
		
        newForm.setResearcherList(project.getResearchers());

		// Set up a Collection of all the Researchers to use in the form as a pull-down menu for researchers
        Collection researchers = Projects.getAllResearchers();
        request.getSession().setAttribute("researchers", researchers);

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
        request.getSession().setAttribute("labDirectors", labDirectors);

		// Go!
		return mapping.findForward(forwardStr);

	}
	
}