/*
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project;

import org.apache.struts.action.*;
import org.uwpr.AppProperties;
import org.uwpr.htpasswd.HTPasswdUserUtils;
import org.uwpr.www.EmailUtils;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller class for editing a project.
 */
public class SaveResearcherAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {

		String firstName;
		String lastName;
		String email;
		String degree;
		String department;
		String organization;
		String state;
		String zip;
		String country;
		boolean sendEmail;
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}

		
		// The Researcher we're creating.
		Researcher researcher = new Researcher();

		
		// Set our variables from the form
		firstName = ((EditResearcherForm)form).getFirstName();
		lastName = ((EditResearcherForm)form).getLastName();
		email = ((EditResearcherForm)form).getEmail();
		degree = ((EditResearcherForm)form).getDegree();
		department = ((EditResearcherForm)form).getDepartment();
		organization = ((EditResearcherForm)form).getOrganization();
		state = ((EditResearcherForm)form).getState();
		zip = ((EditResearcherForm)form).getZipCode();
		country = ((EditResearcherForm)form).getCountry();
		sendEmail = ((EditResearcherForm)form).getSendEmail();
		
		// Set any empty variables to null
		// Only possible empty value is zip code
		if (zip.equals("")) { zip = null; }
		
		// If they're changing their email addy, make sure the NEW one isn't already in the database
		if (UserUtils.emailExists(email) != -1) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.researcher.emailtaken"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}
		
		// Now set the values in the researcher object
		researcher.setFirstName(firstName);
		researcher.setLastName(lastName);
		researcher.setEmail(email);
		researcher.setDegree(degree);
		researcher.setDepartment(department);
		researcher.setOrganization(organization);
		researcher.setState(state);
		researcher.setZipCode(zip);
		researcher.setCountry(country);
		
		// Prepare and send the email
		User newUser = null;
		if (sendEmail) {
			// Generate a new password
			String newPassword = UserUtils.generatePassword();
			String newUsername = email;
			newUser = new User();

			// Set the password in the user and save it.
			newUser.setUsername(newUsername);
			newUser.setPassword(newPassword);

			// add them to the htpasswd file
			try { HTPasswdUserUtils.getInstance().addUser( newUsername, newPassword); }
			catch (Exception e) { ; }
			
			// Generate and send the email to the user.
			try {

			   // set the to address
				Address[] toAddress = InternetAddress.parse(email);

			   // set the message body
				String text = "Greetings " + firstName + " " + lastName + ",\n\n";

				text += "A new account has been created for you at " + AppProperties.getHost() + " by\n";
				text += ((Researcher)(user.getResearcher())).getFirstName() + " ";
				text += ((Researcher)(user.getResearcher())).getLastName() + ".\n\n";
				text += "You can log into the site using the username and password given below to\n";
				text += "manage data for projects with which you are affiliated.\n\n";
				text += "You can also use this username and password to request new collaborations\n";
				text += "with the UW Proteomics Resource.\n\n";
				text += "Your login information:\n";
				text += "Username: " + newUsername + "\n";
				text += "Password: " + newPassword + "\n\n";
				text += "Thank you,\nThe UW Proteomics Resource\n";

			   // send the message
				EmailUtils.sendMail("UWPR Registration Information", text, toAddress);
		   }
			catch (Exception e)
			{
				ActionErrors errors = new ActionErrors();
				errors.add("email", new ActionMessage("error.email.message", "new account", e.toString()));
				saveErrors( request, errors );
				return mapping.findForward("Failure");
			}
		}



		// Save the researcher to the database
		try {
			researcher.save();
		} catch (Exception e) {
			// Invalid email address format
			ActionErrors errors = new ActionErrors();
			errors.add("email", new ActionMessage("error.researcher.saveerror"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}

		if (newUser != null) {
			newUser.setResearcher(researcher);
			try { newUser.save(); }
			catch (Exception e) {
				// Invalid email address format
				ActionErrors errors = new ActionErrors();
				errors.add("email", new ActionMessage("error.researcher.usersaveerror"));
				saveErrors( request, errors );
				return mapping.findForward("Failure");
			}
		}
		
		request.setAttribute("saved", "true");

		// Go!
		return mapping.findForward("Success");


	}
	
}