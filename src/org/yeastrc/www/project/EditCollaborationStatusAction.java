/**
 * EditCollaborationStatus.java
 * @author Vagisha Sharma
 * Apr 1, 2009
 * @version 1.0
 */
package org.yeastrc.www.project;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.Collaboration;
import org.yeastrc.project.CollaborationStatus;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.project.RejectionCauseDAO;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * 
 */
public class EditCollaborationStatusAction extends Action {

    
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
        
        EditCollaborationStatusForm myForm = new EditCollaborationStatusForm();
        myForm.setID(project.getID());
        myForm.setTitle(project.getTitle());
        myForm.setStatus(project.getCollaborationStatus());
        if(project.getDateAccepted() != null)
            myForm.setDateAccepted(project.getDateAccepted().toString());

        
        if(project.isRejected()) {
            List<Integer> rCauses = RejectionCauseDAO.instance().findProjectRejectionCauseIds(projectId);
            String[] causeIds = new String[rCauses.size()];
            int i = 0;
            for(Integer id: rCauses) {
                causeIds[i++] = String.valueOf(id);
            }
            myForm.setRejectionCauses(causeIds);
        }
        
        request.setAttribute("editCollaborationStatusForm", myForm);
        request.getSession().setAttribute("collabRejectionReasons", RejectionCauseDAO.instance().findAll());
        request.getSession().setAttribute("collabStatusOptions", CollaborationStatus.getDisplayStatusList());
        
        
        return mapping.findForward("Success");
    }

}
