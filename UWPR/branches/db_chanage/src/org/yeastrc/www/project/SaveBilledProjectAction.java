package org.yeastrc.www.project;

import org.apache.struts.action.*;
import org.uwpr.htpasswd.HTAccessFileUtils;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.project.Affiliation;
import org.yeastrc.project.BilledProject;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class SaveBilledProjectAction extends Action {

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
		String scientificQuestion = null;
		String progress = null;
		String publications = null;
		String comments;
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
		
		Affiliation affiliation = null;
		
		
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
		BilledProject project;
		
		try {
			project = (BilledProject)(ProjectFactory.getProject(projectID));
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


		// We're saving!
		title = ((EditBilledProjectForm)(form)).getTitle();
		pi = ((EditBilledProjectForm)(form)).getPI();
		groups = ((EditBilledProjectForm)(form)).getGroups();
		fundingTypes = ((EditBilledProjectForm)(form)).getFundingTypes();
		federalFundingTypes = ((EditBilledProjectForm)(form)).getFederalFundingTypes();
		projectAbstract = ((EditBilledProjectForm)(form)).getAbstract();
		scientificQuestion = ((EditBilledProjectForm)(form)).getScientificQuestion();
		progress = ((EditBilledProjectForm)(form)).getProgress();
		publications = ((EditBilledProjectForm)(form)).getPublications();
		comments = ((EditBilledProjectForm)(form)).getComments();
		foundationName = ((EditBilledProjectForm)(form)).getFoundationName();
		grantNumber = ((EditBilledProjectForm)(form)).getGrantNumber();
		grantAmount = ((EditBilledProjectForm)(form)).getGrantAmount();
		
		ltqRunsRequested = ((EditBilledProjectForm)(form)).getLtqRunsRequested();
		ltq_etdRunsRequested = ((EditBilledProjectForm)(form)).getLtq_etdRunsRequested();
		ltq_orbitrapRunsRequested = ((EditBilledProjectForm)(form)).getLtq_orbitrapRunsRequested();
		ltq_ftRunsRequested = ((EditBilledProjectForm)(form)).getLtq_ftRunsRequested();
		tsq_accessRunsRequested = ((EditBilledProjectForm)(form)).getTsq_accessRunsRequested();
		tsq_vantageRunsRequested = ((EditBilledProjectForm)(form)).getTsq_vantageRunsRequested();
		fragmentationTypes = ((EditBilledProjectForm)(form)).getFragmentationTypes();
		databaseSearchRequested = ((EditBilledProjectForm)(form)).isDatabaseSearchRequested();
		massSpecExpertiseRequested = ((EditBilledProjectForm)(form)).isMassSpecExpertiseRequested();
		
		affiliation = ((EditBilledProjectForm)form).getAffiliation();
		
		// Set blank items to null
		if (title.equals("")) title = null;
		if (projectAbstract.equals("")) projectAbstract = null;
		if (comments.equals("")) comments = null;
		
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
		
		
		project.setProgress(progress);
		project.setPublications(publications);
		project.setComments(comments);
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
		if(affiliation != Affiliation.UW) {
			// project.setMassSpecExpertiseRequested(massSpecExpertiseRequested);
			// This should be set correctly in the form, but just in case...
			project.setMassSpecExpertiseRequested(true);
		}
		else {
			project.setMassSpecExpertiseRequested(massSpecExpertiseRequested);
		}
			
		
		// first clear any fragmentation types set for this project;
		project.setCidFragmentation( false );
        project.setPqdFragmentation( false );
        project.setHcdFragmentation( false );
        project.setEtdFragmentation( false );
        project.setEcdFragmentation( false );
        project.setIrmpdFragmentation( false );
        
        
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
		
		project.setAffiliation(affiliation);
		
		// Save the project
		project.save();
		
		
		// make sure htaccess file is current
		try {
			HTAccessFileUtils.getInstance().refreshAllHTAccessFiles( project );
		} catch (Exception e) { ; }

		// Go!
		ActionForward fwd = new ActionForward(mapping.findForward("viewProject").getPath()+"?ID="+project.getID(), true);
		return fwd;
	}
}