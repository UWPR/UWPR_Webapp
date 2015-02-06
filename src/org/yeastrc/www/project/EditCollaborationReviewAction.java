/**
 * EditCollaborationReviewAction.java
 * @author Vagisha Sharma
 * Mar 31, 2009
 * @version 1.0
 */
package org.yeastrc.www.project;

import org.apache.struts.action.*;
import org.yeastrc.project.*;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 */
public class EditCollaborationReviewAction extends Action {


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

        // Make sure user is either a reviewer or administrator
        Groups groupMan = Groups.getInstance();
        if (!groupMan.isMember(user.getResearcher().getID(), "administrators") &&
            !groupMan.isMember(user.getResearcher().getID(), "Researchers")) {
            ActionErrors errors = new ActionErrors();
            errors.add("access", new ActionMessage("error.access.invalidgroup"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        // get the projectID
        int projectId = 0;

        try {
            projectId = Integer.parseInt(request.getParameter("projectId"));

        } catch (NumberFormatException nfe) {
            ActionErrors errors = new ActionErrors();
            errors.add("project", new ActionMessage("error.project.invalidprojectid"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }

        if(projectId == 0) {
            ActionErrors errors = new ActionErrors();
            errors.add("project", new ActionMessage("error.project.invalidprojectid"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        // get the reseacherID of the reviewer
        int reviewerId = 0;
        try {
            reviewerId = Integer.parseInt(request.getParameter("reviewerId"));

        } catch (NumberFormatException nfe) {
            ActionErrors errors = new ActionErrors();
            errors.add("project", new ActionMessage("error.project.invalidreviewerId"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        if(reviewerId == 0) {
            ActionErrors errors = new ActionErrors();
            errors.add("project", new ActionMessage("error.project.invalidreviewerId"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        
        // Load our project
        Collaboration project;

        try {
            project = (Collaboration) ProjectFactory.getProject(projectId);
        } 
        catch (Exception e) {
            // Couldn't load the project.
            ActionErrors errors = new ActionErrors();
            errors.add("project", new ActionMessage("error.project.projectnotfound"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");  
        }
        
        EditCollaborationReviewForm myForm = new EditCollaborationReviewForm();
        myForm.setID(project.getID());
        myForm.setTitle(project.getTitle());
        myForm.setScientificQuestion(project.getScientificQuestion());
        myForm.setAbstract(project.getAbstractAsHTML());
        myForm.setPI(project.getPI().getFirstName()+" "+project.getPI().getLastName()+", "+project.getPI().getDegree());
        List<String> researcherNames = new ArrayList<String>();
        for(Researcher researcher: project.getResearchers())
        {
            researcherNames.add(researcher.getFirstName() + " " + researcher.getLastName() + ", " + researcher.getDegree());
        }
        myForm.setResearcherList(researcherNames);


        myForm.setCollaborationGroups(project.getGroupsString());
        
        myForm.setLtqRunsRequested(project.getLtqRunsRequested());
        myForm.setLtq_ftRunsRequested(project.getLtq_ftRunsRequested());
        myForm.setLtq_orbitrapRunsRequested(project.getLtq_orbitrapRunsRequested());
        myForm.setLtq_etdRunsRequested(project.getLtq_etdRunsRequested());
        myForm.setTsq_accessRunsRequested(project.getTsq_accessRunsRequested());
        myForm.setTsq_vantageRunsRequested(project.getTsq_vantageRunsRequested());
        
        myForm.setFragmentationTypes(project.getFragmentationTypesString());
        
        myForm.setDatabaseSearchRequested(project.isDatabaseSearchRequested());
        myForm.setMassSpecExpertiseRequested(project.isMassSpecExpertiseRequested());
        
        if(project.getParentProjectID() != 0) {
            myForm.setParentProject(project.getParentProjectID());
            myForm.setExtensionReasons(project.getExtensionReasonsAsHTML());
        }
        
        // Get the reviewers for this project
        ProjectReviewerDAO prDao = ProjectReviewerDAO.instance();
        List<ProjectReviewer> reviewers = prDao.getProjectReviewers(((Collaboration)project).getID());
        boolean found = false;
        for(ProjectReviewer reviewer: reviewers) {
            if(reviewer.getResearcher().getID() == reviewerId) {
                addReviewer(myForm, reviewer);
                found = true;
                break;
            }
        }
        if(!found) {
            ActionErrors errors = new ActionErrors();
            String[] ids = new String[0];
            ids[0] = String.valueOf(reviewerId);
            ids[1] = String.valueOf(projectId);
            errors.add("project", new ActionMessage("error.project.reviewerNotFound", ids));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        request.getSession().setAttribute("collabRejectionReasons", RejectionCauseDAO.instance().findAll());
        request.getSession().setAttribute("collabStatusOptions", Arrays.asList(CollaborationStatus.values()));
        request.setAttribute("editCollaborationReviewForm", myForm);
        
        return mapping.findForward("Success");
    }

    private void addReviewer(EditCollaborationReviewForm myForm, ProjectReviewer reviewer) {
        
        myForm.setReviewerId(reviewer.getResearcher().getID());
        myForm.setReviewerName(reviewer.getResearcher().getFirstName()+" "+reviewer.getResearcher().getLastName());
        myForm.setRecommendedStatus(reviewer.getRecommendedStatus());
        if(reviewer.getRecommendedStatus() == CollaborationStatus.REJECTED) {
            
          List<CollaborationRejectionCause> causes = reviewer.getRejectionCauses();
          String[] causeIds = new String[causes.size()];
          int i = 0;
          for(CollaborationRejectionCause cause: causes) {
              causeIds[i++] = String.valueOf(cause.getId());
          }
          myForm.setReviewerRejectionCauseIds(causeIds);
        }
        myForm.setReviewerComments(reviewer.getComment());
        myForm.setEmailComments(reviewer.getEmailComment());
    }
    
}
