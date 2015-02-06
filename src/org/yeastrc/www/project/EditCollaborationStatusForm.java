/**
 * EditCollaborationStatusForm.java
 * @author Vagisha Sharma
 * Apr 1, 2009
 * @version 1.0
 */
package org.yeastrc.www.project;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.CollaborationStatus;

/**
 * 
 */
public class EditCollaborationStatusForm extends ActionForm {

    private int ID; // projectId
    private String title;

    private CollaborationStatus status;
    private String[] rejectionCauses;
    private String dateAccepted;

    private String emailComments;
    
    public String getDateAccepted() {
        return dateAccepted;
    }


    public void setDateAccepted(String dateAccepted) {
        this.dateAccepted = dateAccepted;
    }


    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();

        // If the status recommended by one of the reviewers is "Reject" make sure 
        // at least one rejection reason is selected
        if(status != null && status == CollaborationStatus.REJECTED) {
            if(rejectionCauses == null || rejectionCauses.length == 0) {
                errors.add("project", new ActionMessage("error.project.noRejectionCause"));
            }
        }
        // If the status recommended by the reviewer is "Revise" make sure that 
        // comments have been entered
        if(status != null && status == CollaborationStatus.REVISE) {
            if(emailComments == null || emailComments.length() == 0) {
                errors.add("project", new ActionMessage("error.project.noEmailComments"));
            }
        }
        

        return errors;
    }


    public int getID() {
        return ID;
    }

    public void setID(int id) {
        ID = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CollaborationStatus getStatus() {
        return status;
    }

    public void setStatus(CollaborationStatus status) {
        this.status = status;
    }

    public String getStatusShortName() {
        return status.getShortName();
    }
    
    public void setStatusShortName(String status) {
        this.status = CollaborationStatus.statusForString(status);
    }
    
    public String[] getRejectionCauses() {
        return rejectionCauses;
    }

    public void setRejectionCauses(String[] rejectionCauses) {
        this.rejectionCauses = rejectionCauses;
    }
    
    public String getEmailComments() {
        return emailComments;
    }
    
    public void setEmailComments(String emailComments) {
        this.emailComments = emailComments;
    }
}
