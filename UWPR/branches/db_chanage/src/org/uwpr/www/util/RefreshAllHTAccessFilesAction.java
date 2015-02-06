/*
 * RefreshAllHTAccessFilesAction.java
 * Michael Riffle <mriffle@u.washington.edu>
 * Apr 10, 2008
 */
package org.uwpr.www.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.yeastrc.project.Project;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.uwpr.data.DataURI;
import org.uwpr.data.DataURISaver;
import org.uwpr.data.DataURISearcher;
import org.uwpr.htpasswd.HTAccessFileUtils;
import org.uwpr.www.data.DataProjectSearcher;

/**
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @date Apr 10, 2008
 * Description of class here.
 */
public class RefreshAllHTAccessFilesAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {
	
		// attempt to update the .htaccess file for all external data directories for all projects
		for ( Project p : DataProjectSearcher.getInstance().getProjectsWithExternalData() ) {
			
			// first change the directory in the database to the new directory
			for ( DataURI duri : DataURISearcher.getInstance().searchByProject( p ) ) {
				duri.setUri( duri.getUri().replace( "/nfs/vol1/ProteomicsResource", "/net/pr/vol1/ProteomicsResource" ) );
				DataURISaver.getInstance().save( duri );
			}
			
			// now update the .htaccess file in all of these directories to point to the new location for the password file
			try { HTAccessFileUtils.getInstance().refreshAllHTAccessFiles( p ); }
			catch (Exception e) { ; }
		}
				
		return null;
	}
}
