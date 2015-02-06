package org.yeastrc.www.files;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.yeastrc.files.DataFile;
import org.yeastrc.files.DataFileSearcher;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * Get a list of files associated with a project or MS run in JSON format
 * 
 * @author Mike
 *
 */
public class GetFileListServiceAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {
		
		String jsoncallback = request.getParameter( "jsoncallback" );
		
		try {

			// Ensure the requesting user has access to this project before producing a list for them
			Project p = null;
			User user = UserUtils.getUser(request);
			
			p = ProjectFactory.getProject( Integer.valueOf( request.getParameter( "id" ) ) );

			if( !p.checkReadAccess( user.getResearcher() ) ) {
				JSONObject jobj = new JSONObject();
				jobj.put( "result", "failure" );
				jobj.put( "message", "Access Denied" );

				request.setAttribute( "textToPrint", jsoncallback + "(" + jobj.toString() + ")" );

				return mapping.findForward( "Success" );
			}

			List<DataFile> files = DataFileSearcher.getInstance().getDataFiles( p );
			
			// assemble and return publications as JSON data
			//use JSON.simple library to wrap up the data into JSON (http://code.google.com/p/json-simple/)
			JSONArray datafiles = new JSONArray();
			
			for( DataFile file : files ) {
				
				JSONObject obj=new JSONObject();
				
				obj.put( "filename", file.getFilename() );
				obj.put( "filesize", file.getFilesize() );
				obj.put( "description", file.getDescription() );
				obj.put( "mimetype", file.getMimetype() );
				obj.put( "id", file.getId() );
				obj.put( "uploadDate", file.getTimestamp().toString() );
				obj.put( "uploaderID", file.getUploader().getID() );
				obj.put( "uploaderName", file.getUploader().getFirstName() + " " + file.getUploader().getLastName() );

				datafiles.add( obj );
			}
					
			JSONObject jobj = new JSONObject();
			jobj.put( "data", datafiles);
			
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

			return mapping.findForward( "Success" );
		}
		
		return mapping.findForward( "Success" );
	}
}
