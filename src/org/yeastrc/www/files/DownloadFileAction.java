package org.yeastrc.www.files;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.yeastrc.files.DataFile;
import org.yeastrc.files.DataFileFactory;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

public class DownloadFileAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {
		
		// Ensure the requesting user has access to this project before deleting the requested file
		Project p = null;
		DataFile file = DataFileFactory.getInstance().getDataFile( Integer.parseInt( request.getParameter( "id" ) ) );
		User user = UserUtils.getUser(request);
		
		p = ProjectFactory.getProject( file.getTypeID() );
		
		if( !p.checkReadAccess( user.getResearcher() ) ) {
			return null;
		}
		

		response.setContentType( file.getMimetype() );
		response.setHeader( "Content-Disposition", "attachment;filename=" + file.getFilename() );
		response.setContentLength( file.getFilesize() );

		ServletOutputStream out = response.getOutputStream();

		try {
			out.write( file.getData()  );
			
		} catch (Exception e ) { ; }
		
		finally {
			if( out != null) { out.close(); out = null; }
		}
		
		return null;
	}

}
