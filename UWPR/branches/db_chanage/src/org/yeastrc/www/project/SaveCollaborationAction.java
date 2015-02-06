/*
 *
 * Created on February 5, 2004
 *
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project;

import org.apache.struts.action.*;
import org.uwpr.htpasswd.HTAccessFileUtils;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.project.*;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for saving a project.
 */
public class SaveCollaborationAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {
		int projectID;			// Get the projectID they're after
		
		// The form elements we're after
		String title = null;
		int pi = 0;
		String[] groups = null;
		String[] fundingTypes = null;
		String[] federalFundingTypes = null;
		String projectAbstract = null;
		String publicAbstract = null;
		String collabStatusShortName;
		String scientificQuestion = null;
		String progress = null;
		//String keywords = null;
		String publications = null;
		String comments;
		String instrumentTimeExpl;
		float bta = (float)0.0;
		String axisI = null;
		String axisII = null;
		String grantNumber = null;
		String grantAmount = null;
		String foundationName = null;

		// UWPR stuff
		int ltqRunsRequested = 0;
		int ltq_etdRunsRequested = 0;
		int ltq_orbitrapRunsRequested = 0;
		int ltq_ftRunsRequested = 0;
		int tsq_accessRunsRequested = 0;
		int tsq_vantageRunsRequested = 0;
		
		String[] fragmentationTypes = null;
		
		boolean databaseSearchRequested = false;
		boolean massSpecExpertiseRequested = true;
		
		String extensionReasons = null;
		
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
		Collaboration project;
		
		try {
			project = (Collaboration)(ProjectFactory.getProject(projectID));
			if (!project.checkAccess(user.getResearcher())) {
				
				// This user doesn't have access to this project.
				ActionErrors errors = new ActionErrors();
				errors.add("username", new ActionMessage("error.project.noaccess"));
				saveErrors( request, errors );
				return mapping.findForward("Failure");
			}
		} catch (Exception e) {
			
			// Couldn't load the project.
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.project.projectnotfound"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");	
		}

		// Set this project in the request, as a bean to be displayed on the view
		//request.setAttribute("project", project);

		// We're saving!
		title = ((EditCollaborationForm)(form)).getTitle();
		pi = ((EditCollaborationForm)(form)).getPI();
		groups = ((EditCollaborationForm)(form)).getGroups();
		fundingTypes = ((EditCollaborationForm)(form)).getFundingTypes();
		federalFundingTypes = ((EditCollaborationForm)(form)).getFederalFundingTypes();
		projectAbstract = ((EditCollaborationForm)(form)).getAbstract();
		publicAbstract = ((EditCollaborationForm)(form)).getPublicAbstract();
		scientificQuestion = ((EditCollaborationForm)(form)).getScientificQuestion();
		collabStatusShortName = ((EditCollaborationForm)(form)).getCollaborationStatusShortName();
		//keywords = ((EditCollaborationForm)(form)).getKeywords();
		progress = ((EditCollaborationForm)(form)).getProgress();
		publications = ((EditCollaborationForm)(form)).getPublications();
		comments = ((EditCollaborationForm)(form)).getComments();
		instrumentTimeExpl = ((EditCollaborationForm)(form)).getInstrumentTimeExpl();
		bta = ((EditCollaborationForm)(form)).getBTA();
		axisI = ((EditCollaborationForm)(form)).getAxisI();
		axisII = ((EditCollaborationForm)(form)).getAxisII();
		foundationName = ((EditCollaborationForm)(form)).getFoundationName();
		grantNumber = ((EditCollaborationForm)(form)).getGrantNumber();
		grantAmount = ((EditCollaborationForm)(form)).getGrantAmount();
		
		ltqRunsRequested = ((EditCollaborationForm)(form)).getLtqRunsRequested();
		ltq_etdRunsRequested = ((EditCollaborationForm)(form)).getLtq_etdRunsRequested();
		ltq_orbitrapRunsRequested = ((EditCollaborationForm)(form)).getLtq_orbitrapRunsRequested();
		ltq_ftRunsRequested = ((EditCollaborationForm)(form)).getLtq_ftRunsRequested();
		tsq_accessRunsRequested = ((EditCollaborationForm)(form)).getTsq_accessRunsRequested();
		tsq_vantageRunsRequested = ((EditCollaborationForm)(form)).getTsq_vantageRunsRequested();
		fragmentationTypes = ((EditCollaborationForm)(form)).getFragmentationTypes();
		databaseSearchRequested = ((EditCollaborationForm)(form)).isDatabaseSearchRequested();
		massSpecExpertiseRequested = ((EditCollaborationForm)(form)).isMassSpecExpertiseRequested();
		
		// !!!!!!!!!!!!!!!!!!!!! PROJECT EXTENSION REASON !!!!!!!!!!!!!!!!!!!!!!!
		extensionReasons = ((EditCollaborationForm)(form)).getExtensionReasons();
		
		// Set blank items to null
		if (title.equals("")) title = null;
		if (projectAbstract.equals("")) projectAbstract = null;
		//if (keywords.equals("")) keywords = null;
		//if (progress.equals("")) progress = null;
		//if (publications.equals("")) publications = null;
		if (comments.equals("")) comments = null;
		if (instrumentTimeExpl.equals("")) instrumentTimeExpl = null;
		//if (axisI != null && axisI.equals("")) axisI = null;
		//if (axisII != null && axisII.equals("")) axisII = null;
		
		// Set up our researchers
        Researcher oPI = null;
        try
        {
            if (pi != 0) {
                oPI = new Researcher();
                oPI.load(pi);
            }
            List<Researcher> rList = ((EditProjectForm)(form)).getResearcherList();
            List<Researcher> projResearchers = new ArrayList<Researcher>();
            for(Researcher r: rList) {
                if(r != null && r.getID() > 0) {
                    r.load(r.getID());
                    projResearchers.add(r);
                }
            }
            project.setResearchers(projResearchers);
        }
        catch (InvalidIDException iie) {

            // Couldn't load the researcher.
            ActionErrors errors = new ActionErrors();
            errors.add("project", new ActionMessage("error.project.invalidresearcher"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }

		// Set up the funding types
		project.clearFundingTypes();
		
		if (fundingTypes != null) {
			if (fundingTypes.length > 0) {
				for (int i = 0; i < fundingTypes.length; i++) {
					project.setFundingType(fundingTypes[i]);
				}
			}
		}
		
		// Set up the federal funding types
		project.clearFederalFundingTypes();
		
		if (federalFundingTypes != null) {
			if (federalFundingTypes.length > 0) {
				for (int i = 0; i < federalFundingTypes.length; i++) {
					project.setFederalFundingType(federalFundingTypes[i]);
				}
			}
		}

		// Set up the groups
		project.clearGroups();
		
		if (groups != null) {
			if (groups.length > 0) {
				for (int i = 0; i < groups.length; i++) {
					try { project.setGroup(groups[i]); }
					catch (InvalidIDException iie) {
					
						// Somehow got an invalid group...
						ActionErrors errors = new ActionErrors();
						errors.add("project", new ActionMessage("error.project.invalidgroup"));
						saveErrors( request, errors );
						return mapping.findForward("Failure");					
					}
				}
			}
		}

		// Set all of the new values in the project
		project.setTitle(title);
		project.setPI(oPI);
		project.setAbstract(projectAbstract);
		project.setPublicAbstract(publicAbstract);
		project.setScientificQuestion(scientificQuestion);
		if (collabStatusShortName != null && collabStatusShortName.length() > 0) {
		    project.setCollaborationStatus(collabStatusShortName);
		}
		
		//project.setKeywords(keywords);
		project.setProgress(progress);
		project.setPublications(publications);
		project.setComments(comments);
		project.setInstrumentTimeExpl(instrumentTimeExpl);
		project.setBTA(bta);
		project.setAxisI(axisI);
		project.setAxisII(axisII);
		project.setGrantAmount( grantAmount );
		project.setGrantNumber( grantNumber );
		project.setFoundationName( foundationName );
		
		project.setLtqRunsRequested(ltqRunsRequested);
		project.setLtq_etdRunsRequested(ltq_etdRunsRequested);
		project.setLtq_orbitrapRunsRequested(ltq_orbitrapRunsRequested);
		project.setLtq_ftRunsRequested(ltq_ftRunsRequested);
		project.setTsq_accessRunsRequested(tsq_accessRunsRequested);
		project.setTsq_vantageRunsRequested(tsq_vantageRunsRequested);
		project.setDatabaseSearchRequested(databaseSearchRequested);
		project.setMassSpecExpertiseRequested(massSpecExpertiseRequested);
		
		// first clear any fragmentation types set for this project;
		project.setCidFragmentation( false );
        project.setPqdFragmentation( false );
        project.setHcdFragmentation( false );
        project.setEtdFragmentation( false );
        project.setEcdFragmentation( false );
        project.setIrmpdFragmentation( false );
        
        // !!!!!!!!!!!!!!!!!!!!! PROJECT EXTENSION REASON !!!!!!!!!!!!!!!!!!!!!!!
        project.setExtensionReasons(extensionReasons);
        
		if (fragmentationTypes != null) {
			for ( String fragmentationType : fragmentationTypes ) {
				if (fragmentationType.equals( "CID" ) )
					project.setCidFragmentation( true );
				else if (fragmentationType.equals( "PQD" ) )
					project.setPqdFragmentation( true );
				else if (fragmentationType.equals( "HCD" ) )
					project.setHcdFragmentation( true );
				else if (fragmentationType.equals( "ETD" ) )
					project.setEtdFragmentation( true );
				else if (fragmentationType.equals( "ECD" ) )
					project.setEcdFragmentation( true );
				else if (fragmentationType.equals( "IRMPD" ) )
					project.setIrmpdFragmentation( true );
			}
		} 
		
		boolean setToPending = false;
		if(project instanceof Collaboration) {
		    if(((Collaboration)project).getCollaborationStatus() == CollaborationStatus.REVISE) {
		        if(((EditCollaborationForm)(form)).isEmailReviewer()) {
		            setToPending = true;
		        }
		    }
		}
		
		if(setToPending) 
			project.setCollaborationStatus(CollaborationStatus.REVISE_PENDING);
		
		// Save the project
		project.save();
		
		// send email to reviewers if the project's status is REVISE and 
		// the user checked the Email Reviewers ckheckbox.
		if(setToPending) {
			ReviewerEmailUtils.sendAbstractUpdateEmail((Collaboration)project);
			// update the reviewers' recommended status so that they get reminders to review the project
			
			ProjectReviewerDAO prDao = ProjectReviewerDAO.instance();
			List<ProjectReviewer> reviewers = prDao.getProjectReviewers(project.getID());

			for(ProjectReviewer r: reviewers) {
				r.setRecommendedStatus(CollaborationStatus.REVISE_PENDING);
				prDao.saveProjectReviewer(r);
			}
		}
		
		
		// make sure htaccess file is current
		try {
			HTAccessFileUtils.getInstance().refreshAllHTAccessFiles( project );
		} catch (Exception e) { ; }

		// Go!
		ActionForward fwd = new ActionForward(mapping.findForward("viewProject").getPath()+"?ID="+project.getID(), true);
		return fwd;
	}
}