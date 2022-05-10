/**
 * 
 */
package org.uwpr.www.data;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.data.DataURI;
import org.uwpr.data.DataURISaver;
import org.uwpr.htpasswd.HTAccessFileUtils;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * @author Mike
 *
 */
public class SaveDataURIAction extends Action {

	private static final Logger log = LogManager.getLogger(SaveDataURIAction.class);

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
		int projectID = ((DataURIForm)(form)).getProjectID();
		String uri = ((DataURIForm)(form)).getUri();
		String comments = ((DataURIForm)(form)).getComments();
		
		try {
			project = ProjectFactory.getProject( projectID );
		} catch (Exception e) { ; }
		
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
		
		if (uri == null || uri.equals( "" )) {
			ActionErrors errors = new ActionErrors();
			errors.add("upload", new ActionMessage("error.upload.no.uri"));
			saveErrors( request, errors );

			ActionForward af = mapping.findForward( "Failure" ) ;
			af = new ActionForward( af.getPath() + "?ID=" + project.getID(), af.getRedirect() ) ;
			return af ;
		}

		DataURI dataURI = new DataURI();
		dataURI.setUri( uri );
		dataURI.setComments( comments );
		dataURI.setProject( project );
		
		try {
			DataURISaver.getInstance().save( dataURI );
			HTAccessFileUtils.getInstance().writeHTAccessFile(project, new URL( uri ) );
		} catch (Exception e)
		{
			log.error("Error saving data URI.", e);
		}
		
		// we had difficulty saving this to the database
		if (dataURI.getId() == 0) {
			ActionErrors errors = new ActionErrors();
			errors.add("upload", new ActionMessage("error.upload.save.error"));
			saveErrors( request, errors );
			
			ActionForward af = mapping.findForward( "Failure" ) ;
			af = new ActionForward( af.getPath() + "?ID=" + project.getID(), af.getRedirect() ) ;
			return af ;
		}
		
		ActionForward af = mapping.findForward( "Success" ) ;
		af = new ActionForward( af.getPath() + "?ID=" + project.getID(), af.getRedirect() ) ;
		return af ;
	}
}
