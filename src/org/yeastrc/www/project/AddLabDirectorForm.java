/**
 * 
 */
package org.yeastrc.www.project;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

/**
 * AddLabDirectorForm.java
 * @author Vagisha Sharma
 * Jul 19, 2010
 * 
 */
public class AddLabDirectorForm extends EditResearcherForm {

	private int researcherId;
	
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();
		
		// If we have a valid researcher ID in the form, the other fields should be blank
		if(getResearcherId() > 0) {
			
			if (this.getEmail() != null && this.getEmail().length() > 1) {
				errors.add("researcher", new ActionMessage("error.labdir.onlyone"));
			}
			else if (this.getFirstName() != null && this.getFirstName().length() > 1) {
				errors.add("researcher", new ActionMessage("error.labdir.onlyone"));
			}
			return errors;
		}
		
		// We don't have a valid researcher ID in the form. The user is trying to add a Lab Director
		// that is not already a registered user.
		errors = super.validate(mapping, request);

		return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		setSendEmail(true); // this should always be true. Since we do not display this property it will be false when the form is parsed.
	}

	public int getResearcherId() {
		return researcherId;
	}

	public void setResearcherId(int researcherId) {
		this.researcherId = researcherId;
	}
}
