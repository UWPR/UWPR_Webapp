/* FrontPageAction.java
 * Created on Jun 23, 2004
 */
package org.yeastrc.www.project;

import java.util.Collection;
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
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;


/**
 * Controller class for viewing all highlights.
 */
public class FrontPageAction extends Action {

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

		// Get all NEW projects for this YRC member
		Collection c = user.getNewProjects();
		request.setAttribute("newProjects", c);

		// Get all projects for this YRC user
		c = user.getProjects();
		request.setAttribute("userProjects", c);

		Groups groupMan = Groups.getInstance();
		if (groupMan.isMember(user.getResearcher().getID(), "administrators")) {

		}
		
		// Get all collaborations to be reviewed by this user (if this user is a reviewer)
		if(groupMan.isMember(user.getResearcher().getID(), "Reviewers")) {
		    List<Collaboration> collabs = user.getCollaborationsToReview();
		    request.setAttribute("reviewAssignments", collabs);
		}
        
		// Go!
		return mapping.findForward("Success");
	}	
}
