package org.yeastrc.www.files;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class UploadFileFormAction extends Action {	

	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward execute( ActionMapping mapping,
			ActionForm actionForm,
			HttpServletRequest request,
			HttpServletResponse response )
		throws Exception {
		
		UploadFileForm uff = new UploadFileForm();
		uff.setId( Integer.parseInt( request.getParameter( "id" ) ) );
		uff.setType( request.getParameter( "type" ) );
		
		request.setAttribute( "uploadFileForm", uff );
		
		
		return mapping.findForward( "Success" );
	}

}
