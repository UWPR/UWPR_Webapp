package org.yeastrc.www.files;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.json.simple.JSONObject;

import org.yeastrc.files.DataFile;
import org.yeastrc.files.DataFileDeleter;
import org.yeastrc.files.DataFileFactory;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

public class DeleteFileServiceAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {
		
		String jsoncallback = request.getParameter( "jsoncallback" );
		
		try {

			// Ensure the requesting user has access to this project before deleting the requested file
			Project p = null;
			DataFile file = DataFileFactory.getInstance().getDataFile( Integer.parseInt( request.getParameter( "id" ) ) );
			User user = UserUtils.getUser(request);
			
			p = ProjectFactory.getProject( file.getTypeID() );
			
			if( !p.checkReadAccess( user.getResearcher() ) ) {
				JSONObject jobj = new JSONObject();
				jobj.put( "result", "failure" );
				jobj.put( "message", "Access Denied" );

				request.setAttribute( "textToPrint", jsoncallback + "(" + jobj.toString() + ")" );

				return mapping.findForward( "Success" );
			}
			
			DataFileDeleter.getInstance().deleteDataFile( file );
			file = null;
			
			JSONObject jobj = new JSONObject();
			jobj.put( "result", "success" );
			request.setAttribute( "textToPrint", jsoncallback + "(" + jobj.toString() + ")" );

			
			
		} catch ( Exception e ) {
			JSONObject jobj = new JSONObject();
			jobj.put( "result", "exception" );
			jobj.put( "message", e.getMessage() );

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);			
			jobj.put( "stack", sw.toString() );

			request.setAttribute( "textToPrint", jsoncallback + "(" + jobj.toString() + ")" );
		}
		
		return mapping.findForward( "Success" );
	}

}
