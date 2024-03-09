/* NewProjectUtils.java
 * Created on Jun 22, 2004
 */
package org.yeastrc.www.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.util.MessageResources;
import org.uwpr.www.EmailUtils;
import org.yeastrc.project.Project;
import org.yeastrc.project.Researcher;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

/**
 * Add one sentence class summary here.
 * Add class description here
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, Jun 22, 2004
 *
 */
public class NewProjectUtils {

    
    private static final Logger log = LogManager.getLogger(NewProjectUtils.class);
	/**
	 * Sends an email confirmation of the new project request to the Researcher who
	 * made the request.  If an exception is encountered in the process, the system does
	 * nothing.
	 * @param r The researcher who made the request
	 * @param p The project they've created
	 */
	public static void sendEmailConfirmation(Researcher r, Project p, MessageResources mr) {
		
		try {
			// set the to address
			Address[] toAddress = InternetAddress.parse(r.getEmail());

			// set the message body
			String text = r.getFirstName() + " " + r.getLastName() + ",\n\n";
			text += "Your " + p.getLongType() + " request has been successfully submitted to the UW Proteomics Resource.\n\n";
			text += "The proposed project will be reviewed at our monthly UWPR committee meeting, and you will be notified of our decision.\n\n";
			text += "If you do not hear from us within the next thirty days, please follow up with us at the following email addresses:\n\n";
			
			String[] groups = p.getGroupsArray();
			for (int i = 0; i < groups.length; i++) {
				
				text += groups[i] + " Group (" + mr.getMessage("email.groups." + groups[i]) + ")\n";
			}			

			
			text += "\nThank you,\nThe UW Proteomics Resource\n";

			// send the message
			EmailUtils.sendMail("UWPR - New " + p.getLongType() + " confirmation.", text, toAddress);

		} catch (Exception e) { log.error("Error sending confirmation email to researcher.", e); }
	}
	
}