package org.yeastrc.www.files;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import org.yeastrc.files.DataFile;
import org.yeastrc.files.DataFileDataUtils;
import org.yeastrc.files.DataFileSaver;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

public class AddFileAction extends Action {	

	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward execute( ActionMapping mapping,
			ActionForm actionForm,
			HttpServletRequest request,
			HttpServletResponse response )
		throws Exception {

		User user = UserUtils.getUser(request);
		UploadFileForm form = (UploadFileForm)actionForm;
		Project p = ProjectFactory.getProject( form.getId() );
		
		// no access to this project
		if( !p.checkReadAccess( user.getResearcher() ) ) {
			return mapping.findForward( "Failure" );
		}
		
		// save data file metadata to the database
		FormFile upload = form.getDataFile();
		
		DataFile file = new DataFile();
		file.setUploader( user.getResearcher() );
		file.setFilename( upload.getFileName() );
		file.setFilesize( upload.getFileSize() );
		file.setMimetype( upload.getContentType() );
		
		String type = DataFileDataUtils.PROJECT;
		int tid = p.getID();
						
		DataFileSaver.getInstance().saveDataFile( file, type, tid );
		
		// save the raw data to the database		
		file.setData( upload.getInputStream() );
		
		// kick it
		return mapping.findForward( "Success" );
	}

}
