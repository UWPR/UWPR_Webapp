/*
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.account;

import java.util.*;
import javax.servlet.http.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;

import org.yeastrc.www.user.*;
import org.uwpr.htpasswd.*;
/**
 * Controller class for editing a project.
 */
public class SavePasswordAction extends Action {

	private static final Logger log = LogManager.getLogger(SavePasswordAction.class);

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {

		String password;
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("password", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}
		
		// Set our variables from the form
		password = ((EditPasswordForm)form).getPassword();
		
		// Now set the values in the user object
		user.setPassword(password);
		user.setLastPasswordChange(new Date());
		
		// Save the user to the database
		user.save();
		
		// update this password in the htpasswd file
		try { HTPasswdUserUtils.getInstance().addUser( user.getUsername(), password ); }
		catch (Exception e) {
			log.error("Password could not be saved to " + HTPasswdUtils.PASSWORD_FILE, e);
		}

		request.setAttribute("saved", "true");

		// Go!
		return mapping.findForward("Success");


	}
	
}