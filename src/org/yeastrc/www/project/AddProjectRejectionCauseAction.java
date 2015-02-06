/**
 * AddProjectRejectionCause.java
 * @author Vagisha Sharma
 * Feb 24, 2009
 * @version 1.0
 */
package org.yeastrc.www.project;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.CollaborationRejectionCause;
import org.yeastrc.project.RejectionCauseDAO;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * 
 */
public class AddProjectRejectionCauseAction extends Action {

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

        ProjectRejectionCauseForm myForm = (ProjectRejectionCauseForm) form;
        
        String cause = myForm.getCause();
        if(cause == null || cause.length() == 0)  {
            ActionErrors errors = new ActionErrors();
            errors.add("rejectioncause", new ActionMessage("error.rejectioncause.invalid", "cause"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        String description = myForm.getDescription();
        if(description == null || description.length() == 0)
            description = cause;
       
        
        CollaborationRejectionCause rCause = new CollaborationRejectionCause();
        rCause.setCause(cause);
        rCause.setDescription(description);
        int id = RejectionCauseDAO.instance().addNewRejectionCause(rCause);
        rCause.setId(id);
        request.setAttribute("rejectionReason", rCause);
        
        return mapping.findForward("Success");
    }
}
