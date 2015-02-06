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
import org.uwpr.data.DataURI;
import org.uwpr.data.DataURIDeleter;
import org.uwpr.data.DataURIFactory;
import org.yeastrc.project.Project;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * @author Mike
 *
 */
public class DeleteDataURIAction extends Action {

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

		Project project = null;
		DataURI dataURI = null;
		
		try {
			dataURI = DataURIFactory.getInstance().getDataURI( Integer.parseInt( request.getParameter( "id" ) ) );
			project = dataURI.getProject();
		} catch (Exception e) {
			ActionErrors errors = new ActionErrors();
			errors.add("upload", new ActionMessage("error.upload.invalid.dataURI"));
			saveErrors( request, errors );
			
			ActionForward af = mapping.findForward( "Failure" ) ;
			af = new ActionForward( af.getPath() + "?ID=" + project.getID(), af.getRedirect() ) ;
			return af ;
		}
		
		// could not load the project specified
		if (project == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("upload", new ActionMessage("error.upload.invalid.project"));
			saveErrors( request, errors );
			
			ActionForward af = mapping.findForward( "Failure" ) ;
			af = new ActionForward( af.getPath() + "?ID=" + project.getID(), af.getRedirect() ) ;
			return af ;
		}

		// this user doesn't have access to modify this project
		if (!project.checkAccess( user.getResearcher() ) ) {
			ActionErrors errors = new ActionErrors();
			errors.add("upload", new ActionMessage("error.upload.access.denied"));
			saveErrors( request, errors );

			ActionForward af = mapping.findForward( "Failure" ) ;
			af = new ActionForward( af.getPath() + "?ID=" + project.getID(), af.getRedirect() ) ;
			return af ;
		}

		try {
			DataURIDeleter.getInstance().delete( dataURI );
		} catch (Exception e) {
			ActionErrors errors = new ActionErrors();
			errors.add("upload", new ActionMessage("error.upload.delete.error"));
			saveErrors( request, errors );
			
			ActionForward af = mapping.findForward( "Failure" ) ;
			af = new ActionForward( af.getPath() + "?ID=" + project.getID(), af.getRedirect() ) ;
			return af ;
		}
		
		// remove the htaccess control file from this data URI
		// decided not to do this, as it opens the directory up
		/*
		try {
			HTAccessFileUtils.getInstance().removeHTAccessFile( new URL ( dataURI.getUri() ) );
		} catch (Exception e) { ; }
		*/

		ActionForward af = mapping.findForward( "Success" ) ;
		af = new ActionForward( af.getPath() + "?ID=" + project.getID(), af.getRedirect() ) ;
		return af ;
	}
}
