/**
 * SaveCollaborationStatusAction.java
 * @author Vagisha Sharma
 * Apr 1, 2009
 * @version 1.0
 */
package org.yeastrc.www.project;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.Collaboration;
import org.yeastrc.project.CollaborationStatus;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.project.ProjectReviewer;
import org.yeastrc.project.ProjectReviewerDAO;
import org.yeastrc.project.RejectionCauseDAO;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * 
 */
public class SaveCollaborationStatusAction extends Action {

    private static final Logger log = Logger.getLogger(SaveCollaborationStatusAction.class.getName());
    
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
            !groupMan.isMember(user.getResearcher().getID(), "Reviewers")) {
            ActionErrors errors = new ActionErrors();
            errors.add("access", new ActionMessage("error.access.invalidgroup"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }

        EditCollaborationStatusForm myForm = (EditCollaborationStatusForm) form;
        
        int projectId = myForm.getID();
        CollaborationStatus status = myForm.getStatus();
        List<Integer> rejectCauseIds = new ArrayList<Integer>();
        if(status == CollaborationStatus.REJECTED) {
            String[] rejectionCauses = myForm.getRejectionCauses();
            for(String cause: rejectionCauses) {
                rejectCauseIds.add(Integer.parseInt(cause));
            }
        }
        
        // Load our project
        Collaboration project;
        try {
            project = (Collaboration)(ProjectFactory.getProject(projectId));
        } catch (Exception e) {
            // Couldn't load the project.
            ActionErrors errors = new ActionErrors();
            errors.add("username", new ActionMessage("error.project.projectnotfound"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");  
        }
        
        
        // Get the status stored in the database
        CollaborationStatus oldStatus = project.getCollaborationStatus();
        
        log.info("UPDATING project status. ProjectId: "+projectId+
        		"; old status: "+oldStatus+"; new status: "+status+
        		"; User changing status: "+user.getResearcher().getLastName());
        
        // If the status is REJECTED save the rejection causes
        if(status == CollaborationStatus.REJECTED && rejectCauseIds.size() > 0) {
            List<Integer> reviewerIds = ProjectReviewerDAO.instance().getProjectReviewerIds(projectId);
            RejectionCauseDAO rcdao = RejectionCauseDAO.instance();
            for(int id: reviewerIds) {
                rcdao.saveProjectRejectionCauses(projectId, id, rejectCauseIds);
            }
        }
        
        // If the status is REVISE save the email comments
        if(status == CollaborationStatus.REVISE) {
            List<ProjectReviewer> reviewers = ProjectReviewerDAO.instance().getProjectReviewers(projectId);
            String emailComments = myForm.getEmailComments();
            for(ProjectReviewer reviewer: reviewers) {
                String oldComments = reviewer.getEmailComment();
                if(oldComments != null && oldComments.length() > 0)
                    oldComments += "\n\n";
                else
                    oldComments = "";
                reviewer.setEmailComment(oldComments+emailComments);
                ProjectReviewerDAO.instance().saveProjectReviewer(reviewer);
            }
        }
        
        // save the collaboration status
        project.setCollaborationStatus(status);
        project.save();
        
        
        // compare the old status with the current status of the project
        // send email if the status has changed
        if(oldStatus != project.getCollaborationStatus()) {
            
            if(oldStatus == CollaborationStatus.PENDING ||
               oldStatus == CollaborationStatus.REVISE_PENDING) {
                if(project.getCollaborationStatus() == CollaborationStatus.ACCEPTED) {
                    CollaborationStatusChangeEmailer.sendCollaborationAcceptedEmail(project);
                }
                else if(project.getCollaborationStatus() == CollaborationStatus.REVISE) {
                    CollaborationStatusChangeEmailer.sendRevisionRequestedEmail(project);
                }
                else if(project.getCollaborationStatus() == CollaborationStatus.REJECTED) {
                    CollaborationStatusChangeEmailer.sendCollaborationRejectedEmail(project);
                }
            }
            else if (oldStatus == CollaborationStatus.REVISE) {
                if(project.getCollaborationStatus() == CollaborationStatus.ACCEPTED) {
                    CollaborationStatusChangeEmailer.sendCollaborationAcceptedEmail(project);
                }
                else if(project.getCollaborationStatus() == CollaborationStatus.REJECTED) {
                    CollaborationStatusChangeEmailer.sendCollaborationRejectedEmail(project);
                }
            }
            else if (oldStatus == CollaborationStatus.ACCEPTED) {
                if(project.getCollaborationStatus() == CollaborationStatus.COMPLETE) {
                    CollaborationStatusChangeEmailer.sendCollaborationCompletedEmail(project);
                }
                else if(project.getCollaborationStatus() == CollaborationStatus.EXPIRED) {
                    CollaborationStatusChangeEmailer.sendCollaborationExpiredEmail(project);
                }
            }
        }
        
        // Go!
        ActionForward forward = new ActionForward();
        forward.setPath(mapping.findForward("Success").getPath()+"?ID="+projectId);
        forward.setRedirect(true);
        return forward;
    }

}
