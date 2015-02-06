/**
 * SaveReviewAction.java
 * @author Vagisha Sharma
 * Mar 31, 2009
 * @version 1.0
 */
package org.yeastrc.www.project;

import java.sql.SQLException;
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
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.project.Collaboration;
import org.yeastrc.project.CollaborationRejectionCause;
import org.yeastrc.project.CollaborationStatus;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.project.ProjectReviewer;
import org.yeastrc.project.ProjectReviewerDAO;
import org.yeastrc.project.RejectionCauseDAO;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * 
 */
public class SaveCollaborationReviewAction extends Action{

    
    private static final Logger log = Logger.getLogger(SaveCollaborationReviewAction.class.getName());
    
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

        EditCollaborationReviewForm myForm = (EditCollaborationReviewForm) form;
        
        if(!myForm.isDraft())
            return saveReview(mapping, request, myForm);
        else
            return saveDraft(mapping, request, myForm);
    }

    private ActionForward saveDraft(ActionMapping mapping,
            HttpServletRequest request, EditCollaborationReviewForm myForm)
            throws InvalidIDException, SQLException {
        
        int projectId = myForm.getID();
        int reviewerId = myForm.getReviewerId();
        String comments = myForm.getReviewerComments();
        String emailComment = myForm.getEmailComments();
        
        ProjectReviewer reviewer = new ProjectReviewer();
        Researcher researcher = new Researcher();
        researcher.load(reviewerId);
        reviewer.setResearcher(researcher);
        reviewer.setComment(comments);
        reviewer.setEmailComment(emailComment);
        reviewer.setProjectId(projectId);
       
        log.info("Saving DRAFT review for project: "+projectId+"; reviewer: "+researcher.getLastName()+" ID: "+reviewerId);
        
        // save
        ProjectReviewerDAO.instance().saveProjectReviewer(reviewer);
        
        // Go!
        ActionForward forward = new ActionForward();
        forward.setPath(mapping.findForward("Success").getPath()+"?ID="+projectId);
        forward.setRedirect(true);
        return forward;
    }
    
    private ActionForward saveReview(ActionMapping mapping,
            HttpServletRequest request, EditCollaborationReviewForm myForm)
            throws InvalidIDException, SQLException {
        
        int projectId = myForm.getID();
        int reviewerId = myForm.getReviewerId();
        CollaborationStatus recommendation = myForm.getRecommendedStatus();
        String[] rejectionCause = myForm.getReviewerRejectionCauseIds();
        String comments = myForm.getReviewerComments();
        String emailComment = myForm.getEmailComments();
        
        ProjectReviewer reviewer = new ProjectReviewer();
        Researcher researcher = new Researcher();
        researcher.load(reviewerId);
        reviewer.setResearcher(researcher);
        reviewer.setComment(comments);
        reviewer.setEmailComment(emailComment);
        reviewer.setProjectId(projectId);
        reviewer.setRecommendedStatus(recommendation);
        
        log.info("Saving REVIEW for project: "+projectId+"; reviewer: "+researcher.getLastName()+" ID: "+reviewerId+
        		"; RECOMMENDED STATUS: "+recommendation);
        
        if(recommendation == CollaborationStatus.REJECTED) {
            List<CollaborationRejectionCause> causes = new ArrayList<CollaborationRejectionCause>(rejectionCause.length);
            for(String str: rejectionCause) {
                int id = Integer.parseInt(str);
                CollaborationRejectionCause cause = RejectionCauseDAO.instance().find(id);
                causes.add(cause);
            }
            reviewer.setRejectionCauses(causes);
        }
        
        // save
        ProjectReviewerDAO.instance().saveProjectReviewer(reviewer);
        
        
        
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
        
        
        // Get all reviews for this project
        List<ProjectReviewer> reviewers = ProjectReviewerDAO.instance().getProjectReviewers(projectId);
        if(reviewers == null || reviewers.size() == 0) {
        	log.error("No reviewers found for project: "+projectId);
        }
        
        // Check if all reviewers have now submitted their recommendations
        boolean allReviewsIn = allReviewsSubmitted(reviewers);
        if(allReviewsIn) {
            
        	log.info("ALL reviews are in for project: "+projectId);
        	
            // Update the project status if there is consensus
            // Get the consensus (if any) status of the project's reviewer
            CollaborationStatus consensus = getConsensusStatus(reviewers);
            if(consensus != null) {
                
            	log.info("Consensus status is: "+consensus+" for project: "+projectId);
            	
                if(consensus == CollaborationStatus.ACCEPTED) {
                    project.setCollaborationStatus(CollaborationStatus.ACCEPTED);
                    project.save();
                }
                else if (consensus == CollaborationStatus.REJECTED) {
                    project.setCollaborationStatus(CollaborationStatus.REJECTED);
                    project.save();
                }
                else if (consensus == CollaborationStatus.REVISE) {
                    project.setCollaborationStatus(CollaborationStatus.REVISE);
                    project.save();
                }
                
                // If the original project status was PENDING OR REVISE_PENDING send an email to the researcher
                // notifying her/him of change in status
                if(oldStatus == CollaborationStatus.PENDING ||
                   oldStatus == CollaborationStatus.REVISE_PENDING) {
                    if(consensus == CollaborationStatus.ACCEPTED) {
                        CollaborationStatusChangeEmailer.sendCollaborationAcceptedEmail(project);
                    }
                    else if (consensus == CollaborationStatus.REJECTED) {
                        CollaborationStatusChangeEmailer.sendCollaborationRejectedEmail(project);
                    }
                    else if (consensus == CollaborationStatus.REVISE) {
                        CollaborationStatusChangeEmailer.sendRevisionRequestedEmail(project);
                    }
                }
                
                // If the original project status was REVISE send an email to the researcher
                // notifying her/him of change in status
                // This should not really happen.  A project should go from REVISE to REVISE_PENDING 
                // and then to ACCEPT, REJECT or REVISE. But, just in case this happens....
                if(oldStatus == CollaborationStatus.REVISE) {
                    if(consensus == CollaborationStatus.ACCEPTED) {
                        CollaborationStatusChangeEmailer.sendCollaborationAcceptedEmail(project);
                    }
                    else if (consensus == CollaborationStatus.REJECTED) {
                        CollaborationStatusChangeEmailer.sendCollaborationRejectedEmail(project);
                    }
                }
            }
            // THERE WAS NO CONSENSUS!!! Send mail to Priska and reviewers 
            else {
                
                // IF the current project status is REVISE don't send review conflict email if
                // at least one of the reviewer's recommended status is REVISE
//                if(oldStatus == CollaborationStatus.REVISE && isStillRevise(reviewers)) {
//                    log.info("Review conflict but current status is REVISE and one of the reviewer's recommended status is REVISE");
//                }
//                else {
                    log.info("Reviewer recommendation conflict for project: "+projectId+". Sending email to Priska and reviewers...");
                    ReviewerEmailUtils.sendReviewConflictEmail(project, reviewers);
//                }
            }
        }
        
        // TODO This is temporary
        ReviewerEmailUtils.emailVsharma(project, reviewer);
        
        // Go!
        ActionForward forward = new ActionForward();
        forward.setPath(mapping.findForward("Success").getPath()+"?ID="+projectId);
        forward.setRedirect(true);
        return forward;
    }

    private CollaborationStatus getConsensusStatus(List<ProjectReviewer> reviewers) {
        
        CollaborationStatus consensus = null;
        if(reviewers == null || reviewers.size() == 0)
        	return consensus;
        
        consensus = reviewers.get(0).getRecommendedStatus();
        
        if(reviewers.size() == 1)
        	return consensus;
        
        for(int i = 1; i < reviewers.size(); i++) {
            if(reviewers.get(i).getRecommendedStatus() != consensus)
                return null;
        }
        return consensus;
    }
    
    // Returns true if one of reviewers' recommended status is REVISE
    private boolean isStillRevise(List<ProjectReviewer> reviewers) {
        
        for(int i = 0; i < reviewers.size(); i++) {
            if(reviewers.get(i).getRecommendedStatus() == CollaborationStatus.REVISE)
                return true;
        }
        return false;
    }
    
    private boolean allReviewsSubmitted(List<ProjectReviewer> reviewers) {
        
    	if(reviewers == null || reviewers.size() == 0)
    		return false;
    	
        for(ProjectReviewer reviewer: reviewers) {
            if(!reviewer.isReviewSubmitted()) {
                return false;
            }
        }
        return true;
    }
}
