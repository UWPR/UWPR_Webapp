/*
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.AdminUtils;
import org.uwpr.htpasswd.HTPasswdUserUtils;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

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
			   // set the SMTP host property value
			   Properties properties = System.getProperties();
			   properties.put("mail.smtp.host", "localhost");

			   // create a JavaMail session
			   javax.mail.Session mSession = javax.mail.Session.getInstance(properties, null);

			   // create a new MIME message
			   MimeMessage message = new MimeMessage(mSession);

			   // set the from address
			   Address fromAddress = AdminUtils.getFromAddress();
			   message.setFrom(fromAddress);

			   // set the to address
				Address[] toAddress = InternetAddress.parse(email);
				message.setRecipients(Message.RecipientType.TO, toAddress);

			   // set the subject
			   message.setSubject("UWPR Registration Info");

			   // set the message body
				String text = "Greetings " + firstName + " " + lastName + ",\n\n";

				text += "A new account has been created for you at http://proteomicsresource.washington.edu/ by\n";
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

				message.setText(text);

			   // send the message
			   Transport.send(message);

		   }
			catch (AddressException e) {
				// Invalid email address format
				ActionErrors errors = new ActionErrors();
				errors.add("email", new ActionMessage("error.forgotpassword.sendmailerror"));
				saveErrors( request, errors );
				return mapping.findForward("Failure");
			}
			catch (SendFailedException e) {
				// Invalid email address format
				ActionErrors errors = new ActionErrors();
				errors.add("email", new ActionMessage("error.forgotpassword.sendmailerror"));
				saveErrors( request, errors );
				return mapping.findForward("Failure");
			}
			catch (MessagingException e) {
				// Invalid email address format
				ActionErrors errors = new ActionErrors();
				errors.add("email", new ActionMessage("error.forgotpassword.sendmailerror"));
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