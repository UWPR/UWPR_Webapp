/*
 *
 * Created on February 5, 2004
 *
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project;

import org.apache.struts.action.*;
import org.uwpr.data.DataURI;
import org.uwpr.data.DataURISearcher;
import org.uwpr.data.MSDaPlExperimentSearcher;
import org.uwpr.instrumentlog.rawfile.ProjectRawFileUsage;
import org.uwpr.instrumentlog.rawfile.ProjectRawFileUsageUtils;
import org.yeastrc.project.*;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.List;

/**
 * Implements the logic to register a user
 */
public class ViewProjectAction extends Action {

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
			if (!project.checkReadAccess(user.getResearcher())) {
				
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

		
		if(project.getShortType().equals(Projects.COLLABORATION)) {
			// Set this project in the request, as a bean to be displayed on the view
			CollaborationAndReview projAndRev = new CollaborationAndReview();
			projAndRev.setCollaboration((Collaboration)project);
			
			// Get the reviewers for the projects
	        ProjectReviewerDAO prDao = ProjectReviewerDAO.instance();
			List<ProjectReviewer> reviewers = prDao.getProjectReviewers(project.getID());
			projAndRev.setReviewers(reviewers);
			request.setAttribute("projectAndReview", projAndRev);
		}
		
		// Get the ancestor projects(if any)
		List<Integer> ancestorIds = ProjectDAO.instance().getAncestors(project.getID());
		request.setAttribute("ancestorProjects", ancestorIds);
		
		// Get child project (if any)
		int childProjectId = ProjectDAO.instance().getChildProjectId(project.getID());
		request.setAttribute("childProjectId", childProjectId);
		
		
		
		
		// add links to external data to the request
		try
        {
            List<DataURI> externalLinkData = DataURISearcher.getInstance().searchByProject(project);

            // Add any experiments uploaded to the MSDaPl database for this project
            List<DataURI> msdaplExperiments = MSDaPlExperimentSearcher.searchByProject(project);
            externalLinkData.addAll(msdaplExperiments);

            request.setAttribute( "externalLinkData", externalLinkData);
		}
        catch (Exception ignored) {}
		
		
		
		// CAN THIS PROJECT BE EXTENDED
		if(project.getShortType().equals(Projects.COLLABORATION)) { // should always be true for the UWPR
		    Collaboration collab = (Collaboration) project;
		    // Project should be either complete or expired AND have a valid progress report 
		    // AND should not have any child projects.
		    if(((collab.isComplete() || collab.isExpired()) && collab.isProgressReportValid()) && !isExtended(collab)) {
		        request.setAttribute("canExtend", true);
		    }
		    else
		        request.setAttribute("canExtend", false);
		}
		
		// RAW FILE USAGE for the project
		if(project.getShortType().equals(Projects.COLLABORATION)) { // should always be true for the UWPR
			
			ProjectRawFileUsage rawFileUsage = ProjectRawFileUsageUtils.instance().loadUsage(project.getID());
			if(rawFileUsage != null)
				request.setAttribute("rawFileUsage", rawFileUsage);
		}
		

		// Forward them on to the happy success view page!
		if (project.getShortType().equals(Projects.COLLABORATION)) {
		    Collaboration collab = (Collaboration)project;
            
            // emails of all researchers associated with the project
            String emailStr = "";
            if(collab.getPI() != null)
                emailStr += ","+collab.getPI().getEmail();
            for(Researcher researcher: project.getResearchers())
            {
                emailStr += "," + researcher.getEmail();
            }

            if(emailStr.length() > 0)
                emailStr = emailStr.substring(1);
            
		    String subject = "UWPR - collaboration request (ID: "+project.getID()+")";
		    request.setAttribute("investigatorEmail", emailStr);
		    request.setAttribute("emailSubject", subject);
		    
			return mapping.findForward("Collaboration");
		}

		else if(project.getShortType().equals(Projects.BILLED_PROJECT)) {
			request.setAttribute("project", project);
			return mapping.findForward("BilledProject");
		}
		
		ActionErrors errors = new ActionErrors();
		errors.add("username", new ActionMessage("error.project.invalidtype"));
		saveErrors( request, errors );
		return mapping.findForward("Failure");

	}

    private boolean isExtended(Collaboration collab) throws SQLException {
        ProjectDAO projDAO = ProjectDAO.instance();
        return projDAO.getChildProjectId(collab.getID()) > 0;
    }
	
}