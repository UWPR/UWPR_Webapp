/**
 * 
 */
package org.uwpr.www.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;
import org.uwpr.data.*;
import java.io.*;
import java.net.*;

/**
 * @author Mike
 *
 */
public class ViewExternalDataURLAction extends Action {

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
		
		DataURI dataURI = null;
		try {
			dataURI = DataURIFactory.getInstance().getDataURI( request.getParameter( "uri" ) );
		} catch (Exception e) { ; }

		// couldn't get the owning URI object out of the data?  error
		if (dataURI == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("upload", new ActionMessage("error.data.urierror"));
			saveErrors( request, errors );
			return mapping.findForward( "Failure" );
		}

		// nice try, buddy! access denied
		if (!dataURI.getProject().checkReadAccess( user.getResearcher() ) ) {
			ActionErrors errors = new ActionErrors();
			errors.add("upload", new ActionMessage("error.project.noaccess"));
			saveErrors( request, errors );
			return mapping.findForward( "Failure" );
		}
		
		URI uri = new URI( dataURI.getUri() );
		Socket sock = new Socket( uri.getHost(), uri.getPort() );
		
		
		return null;
	}
}
