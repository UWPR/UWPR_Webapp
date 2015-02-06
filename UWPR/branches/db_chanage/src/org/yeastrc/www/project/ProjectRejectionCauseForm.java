package org.yeastrc.www.project;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class ProjectRejectionCauseForm extends ActionForm{
    
    private String cause;
    private String description;
    
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        
        ActionErrors errors = new ActionErrors();
        
        if (cause == null || cause.length() == 0) {
            errors.add("rejectioncause", new ActionMessage("error.rejectioncause.invalid", "cause"));
        }
        return errors;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }
}
