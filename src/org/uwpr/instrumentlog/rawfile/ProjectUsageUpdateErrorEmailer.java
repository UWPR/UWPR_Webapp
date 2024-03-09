/**
 * ProjectUsageUpdateErrorEmailer.java
 * @author Vagisha Sharma
 * Jun 23, 2011
 */
package org.uwpr.instrumentlog.rawfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uwpr.AdminUtils;
import org.uwpr.www.EmailUtils;
import org.yeastrc.project.Researcher;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.util.List;

/**
 * 
 */
public class ProjectUsageUpdateErrorEmailer {

	private static ProjectUsageUpdateErrorEmailer instance = new ProjectUsageUpdateErrorEmailer();
	
	private static final Logger log = LogManager.getLogger(ProjectUsageUpdateErrorEmailer.class);
	
	private ProjectUsageUpdateErrorEmailer() {}
	
	public static ProjectUsageUpdateErrorEmailer getInstance() {
		return instance;
	}
	
	public void sendEmail(String errorMessage) {
		
		log.info("Sending project usage update error email");
		
		List<Researcher> researchers = AdminUtils.getNotifyAdmins();

		try {
			String emailStr = "";
			for(Researcher r: researchers) {
				emailStr += ","+r.getEmail();
			}
			if(emailStr.length() > 0) emailStr = emailStr.substring(1); // remove first comma

			Address[] toAddress = InternetAddress.parse(emailStr);

			// set the message body
			StringBuilder text = new StringBuilder();
			text.append("There was an error while updating project usage.  The error message was: "+errorMessage+"\n");
			text.append("Please check the logs for more details.\n");
			text.append("\n");
			
			text.append("\n\nThank you,\nThe UW Proteomics Resource\n");

			System.out.println(text);

			// send the message
			EmailUtils.sendMail("UWPR - Project usage update error!!!", text.toString(), toAddress);
		}
		catch (Exception e)
		{
			log.error("Could not send email about error encountered while updating project usage." , e);
		}
		
	}
}
