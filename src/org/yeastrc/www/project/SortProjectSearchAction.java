/* SortProjectSearchAction.java
 * Created on May 12, 2004
 */
package org.yeastrc.www.project;

import javax.servlet.http.*;
import org.apache.struts.action.*;
import java.util.*;

import org.yeastrc.project.*;
import org.yeastrc.www.user.*;

/**
 * Add one sentence class summary here.
 * Add class description here
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, May 12, 2004
 *
 */
public class SortProjectSearchAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {
		
		HttpSession session = request.getSession();
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}

		// Make sure we have the pre-existing list of projects from their previous search
		// We can have two types of projects:  Subsidized projects and Billed projects
		
		// Get the subsidized projects if any
		List<CollaborationAndReview> projAndRev;
		try {
		    projAndRev = (List<CollaborationAndReview>)(session.getAttribute("subsidizedProjects"));
		} catch (Exception e) {
			return mapping.findForward("Failure");
		}
		
		// Get the billed projects, if any
		List<BilledProject> billedProjects = null;
		try {
			billedProjects = (List<BilledProject>)(session.getAttribute("billedProjects"));
		}
		catch(Exception e) {
			return mapping.findForward("Failure");
		}
		
		// Figure out how we're sorting the list
		String sortby = request.getParameter("sortby");
		if (sortby == null || sortby.equals("")) {
			return mapping.findForward("Failure");
		}

		if(projAndRev != null) {
			if (sortby.equals("change")) { Collections.sort(projAndRev, new ProjectLastChangeComparator()); }
			else if (sortby.equals("pi")) { Collections.sort(projAndRev, new ProjectPIComparator()); }
			else if (sortby.equals("title")) { Collections.sort(projAndRev, new ProjectTitleComparator()); }
			else if (sortby.equals("submit")) { Collections.sort(projAndRev, new ProjectSubmitDateComparator()); }
			else if (sortby.equals("id")) { Collections.sort(projAndRev, new ProjectIDComparator()); }
			else if (sortby.equals("status")) { Collections.sort(projAndRev, new CollaborationStatusComparator()); }
			else if (sortby.equals("reviewer")) { Collections.sort(projAndRev, new CollaborationReviewerComparator()); }
		}
		if(billedProjects != null) {
			if (sortby.equals("change")) { Collections.sort(billedProjects, new ProjectLastChangeComparator()); }
			else if (sortby.equals("pi")) { Collections.sort(billedProjects, new ProjectPIComparator()); }
			else if (sortby.equals("title")) { Collections.sort(billedProjects, new ProjectTitleComparator()); }
			else if (sortby.equals("submit")) { Collections.sort(billedProjects, new ProjectSubmitDateComparator()); }
			else if (sortby.equals("id")) { Collections.sort(billedProjects, new ProjectIDComparator()); }
		}
		
		return mapping.findForward("Success");
	}
}
