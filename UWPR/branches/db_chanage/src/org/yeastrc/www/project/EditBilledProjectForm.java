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
import org.yeastrc.project.Affiliation;

public class EditBilledProjectForm extends EditProjectForm {

	/** The groups for this collaboration */
	private String[] groups = new String[0];
	
	private Affiliation affiliation;
	
	private boolean editable = true;
	
	/**
	 * Validate the properties that have been sent from the HTTP request,
	 * and return an ActionErrors object that encapsulates any
	 * validation errors that have been found.  If no errors are found, return
	 * an empty ActionErrors object.
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = super.validate(mapping, request);
		
		if(affiliation == null) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionMessage("error.billedproject.noaffiliation"));
		}
		
		return errors;

	}

    /** Set the groups */
	public void setGroups(String[] groups) {
		if (groups != null) { this.groups = groups; }
	}

	/** Get the groups */
	public String[] getGroups() { return this.groups; }

	public void setAffiliation(Affiliation affiliation) {
	    this.affiliation = affiliation;
	}
	
	public Affiliation getAffiliation() {
		return this.affiliation;
	}
	
	public String getAffiliationName() {
		if(affiliation == null)
			return "None";
		else
			return this.affiliation.name();
	}
	
	public void setAffiliationName(String name) {
		this.affiliation = Affiliation.forName(name);
	}
	
	public boolean isEditable() {
	    return editable;
	}
	
	public void setEditable(boolean editable) {
	    this.editable = editable;
	}
}