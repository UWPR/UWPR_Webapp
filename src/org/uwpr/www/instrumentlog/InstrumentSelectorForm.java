package org.uwpr.www.instrumentlog;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class InstrumentSelectorForm extends ActionForm {

	private String instrumentID;
	
	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getInstrumentID() {
		return instrumentID;
	}
	
	public void setInstrumentID(String instrument) {
		this.instrumentID = instrument;
	}
	
	public boolean isFormInitialized() {
		return instrumentID != null;
	}
}
