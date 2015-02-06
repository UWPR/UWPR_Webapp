package org.yeastrc.www.files;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;

public class UploadFileForm extends ActionForm {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7565265525609345907L;

	/**
	 * Validate the properties that have been sent from the HTTP request,
	 * and return an ActionErrors object that encapsulates any
	 * validation errors that have been found.  If no errors are found, return
	 * an empty ActionErrors object.
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {

		ActionErrors errors = new ActionErrors();
	
		if ( dataFile == null  ) {

			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionError("errors.required", "File" ));

		} else {

			if ( StringUtils.isEmpty( dataFile.getFileName() ) ) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionError("errors.required", "File" ));

			} else {

				int fileSize = dataFile.getFileSize();

				if ( fileSize == 0 ) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionError("errors.required", "File" ));
				} else if( fileSize > Math.pow( 10, 7 ) ) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionError( "File must be under 10 megabytes." ) );
				}
			}
		}
		
		return errors;
	}
	
	
	public FormFile getDataFile() {
		return dataFile;
	}

	public void setDataFile(FormFile dataFile) {
		this.dataFile = dataFile;
	}



	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}



	private FormFile dataFile;
	int id;
	String type;
	
}
