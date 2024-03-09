/*
 * ForgotPasswordAction.java
 *
 * Created on October 17, 2003
 *
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.login;

import org.apache.struts.action.*;
import org.uwpr.AppProperties;
import org.uwpr.www.EmailUtils;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.NoSuchUserException;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Implements the logic to register a user
 */
public class ForgotPasswordAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {

		// Get their session first.  Disallow them from logging in if they already are
		HttpSession session = request.getSession();
		session.removeAttribute("user");

		// These items should have already been validated in the ActionForm
		String username = ((ForgotPasswordForm)form).getUsername();
		String email = ((ForgotPasswordForm)form).getEmail();


		// Find the user in the database.
		User user = null;
		if (username != null && !username.equals("")) {
			try {
				user = UserUtils.getUser(username);
			} catch (NoSuchUserException nsue) {
				ActionErrors errors = new ActionErrors();
				errors.add("username", new ActionMessage("error.forgotpassword.invaliduser"));
				saveErrors( request, errors );
				return mapping.findForward("Failure");
			}
		} else {
			// Get the researcherID corresponding to this email address
			int researcherID = UserUtils.emailExists(email);

			if (researcherID == -1) {
				// Email address doesn't exist
				ActionErrors errors = new ActionErrors();
				errors.add("email", new ActionMessage("error.forgotpassword.invalidemail"));
				saveErrors( request, errors );
				return mapping.findForward("Failure");
			} else {
				// Email address does exist
				if (UserUtils.userExists(researcherID)) {

					// This user has been found!  Create a user and load it
					user = new User();
					user.load(researcherID);

				} else {
					// The researcher exists, but not the user.  Create a new user, associate it w/ this researcher
					Researcher researcher = new Researcher();
					researcher.load(researcherID);
					
					user = new User();
					user.setUsername(researcher.getEmail());
					user.setResearcher(researcher);
				}
			}
		}
		
		// We should now have a valid User object
		
		// Generate a new password
		String password = UserUtils.generatePassword();
		
		// Set the password in the user and save it.
		user.setPassword(password);
		user.save();
		
		// Generate and send the email to the user.
        try
		{
			// set the to address
			Address[] toAddress = InternetAddress.parse(user.getResearcher().getEmail());

			// set the message body
			String text = "Your password on " + AppProperties.getHost() + " has been reset. Here is your login information :\n\n";
			text += "Username: " + user.getUsername() + "\n";
			text += "Password: " + password + "\n\n";
			text += "Thank you,\nThe UW Proteomics Resource\n";

			// send the message
			EmailUtils.sendMail("UWPR Login Information", text, toAddress);
		}
		catch (Exception e)
		{
			ActionErrors errors = new ActionErrors();
			errors.add("email", new ActionMessage("error.email.message", "password reset", e.toString()));
			saveErrors( request, errors );e.toString();
			return mapping.findForward("Failure");
		}


		// Forward them on to the happy success page!
		return mapping.findForward("Success");
	}
	
}