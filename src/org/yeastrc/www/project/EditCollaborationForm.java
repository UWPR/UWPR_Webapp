/*
 * RegisterForm.java
 *
 * Created on October 17, 2003
 *
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.CollaborationStatus;

/**
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version 2004-01-21
 */
public class EditCollaborationForm extends EditProjectForm {

	/**
	 * Validate the properties that have been sent from the HTTP request,
	 * and return an ActionErrors object that encapsulates any
	 * validation errors that have been found.  If no errors are found, return
	 * an empty ActionErrors object.
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = super.validate(mapping, request);
		
		
		// User has to provide justification for the requested instrument time
		// If no runs are requested this field should contain explanation for any other
		// requested resources.
		// First check if this is project is currently pending.  We don't need instrument time 
		// justification for older projects that have already been accepted
		if(!this.isNotPending()) { // project is pending
			String instrumentTimeExplanation = this.getInstrumentTimeExpl();
			if(instrumentTimeExplanation == null || 
					instrumentTimeExplanation.trim().length() == 0 ||
					wordCount(this.getInstrumentTimeExpl()) < 1) {
				errors.add("project", new ActionMessage("error.project.noinstrtimeexpl"));
			}
		}
		
		// If this is an extension project we need a reason for the extension
		if(this.getParentProjectID() != 0) {
		    String extReasons = getExtensionReasons();
		    if(extReasons == null || WordCounter.count(extReasons) < 20)
		        errors.add(ActionErrors.GLOBAL_ERROR, new ActionMessage("error.project.noextReasons"));
		}
		
		String[] groups = this.getGroups();
		if (groups == null || groups.length < 1) {
			errors.add("groups", new ActionMessage("error.collaboration.nogroups"));
		}

		return errors;

	}

    /** Set the groups */
	public void setGroups(String[] groups) {
		if (groups != null) { this.groups = groups; }
	}

	/** Set whether or not to send emails */
	public void setSendEmail(boolean arg) {
		this.sendEmail = arg;
	}

	/** Set whether or not this will be saved as a tech dev project */
	public void setIsTech(boolean arg) { this.isTech = arg; }
	
	/** Get the groups */
	public String[] getGroups() { return this.groups; }

	/** Get whether or not to send email */
	public boolean getSendEmail() { return this.sendEmail; }
	
	/** Get whether or not this will be saved as a tech dev project */
	public boolean getIsTech() { return this.isTech; }

	/** Gets the short name for the status of this collaboration */
	public String getCollaborationStatusShortName() {
	    if (collabStatus != null)
	        return collabStatus.getShortName();
	    else
	        return "";
	}
	
	/** Set the status for this collaboration based on the given short name for the status*/
	public void setCollaborationStatusShortName(String shortName) {
	    this.collabStatus = CollaborationStatus.statusForString(shortName);
	}
	
	/** Set the status for this collaboration */
	public void setCollaborationStatus(CollaborationStatus status) {
	    this.collabStatus = status;
	}
	
	public String getDateAccepted() {
	    return dateAccepted;
	}
	
	public void setDateAccepted(String dateAccepted) {
	    this.dateAccepted = dateAccepted;
	}
	
	public boolean isFullEditable() {
	    return fullEditable;
	}
	
	public void setFullEditable(boolean fullEditable) {
	    this.fullEditable = fullEditable;
	}
	
	public boolean isPartEditable() {
		return partEditable;
	}
	
	public void setPartEditable(boolean partEditable) {
		this.partEditable = partEditable;
	}
	
    public String getInformationRequested() {
        return informationRequested;
    }

    public void setInformationRequested(String informationRequested) {
        this.informationRequested = informationRequested;
    }
    
    public boolean isEmailReviewer() {
        return emailReviewer;
    }

    public void setEmailReviewer(boolean emailReviewer) {
        this.emailReviewer = emailReviewer;
    }
    
    public void reset(ActionMapping mapping, javax.servlet.http.HttpServletRequest request) {
        // This needs to be set to false because if a checkbox is not checked the browser does not
        // send its value in the request.
        // http://struts.apache.org/1.1/faqs/newbie.html#checkboxes
        emailReviewer = false;
    }
    
	/** The groups for this collaboration */
	private String[] groups = new String[0];
	
	/** Whether or not to send email to YRC groups */
	private boolean sendEmail = true; // TODO remove this; no longer used
	
	/** Whether or not this is a technology development project */
	private boolean isTech = false;
	
	private CollaborationStatus collabStatus;
	
	private String informationRequested = "";
	
	private boolean emailReviewer = false; // used only if the status is REVISE
	
	
	// these will be un-editable fields in the form.  They have been added to the form so that if an error 
	// happens in form validation these values are still available to display in the form. 
	private String dateAccepted = null;
	
	private boolean fullEditable = false;
	
	private boolean partEditable = false;

}