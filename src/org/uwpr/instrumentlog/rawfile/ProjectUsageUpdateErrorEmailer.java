/**
 * ProjectUsageUpdateErrorEmailer.java
 * @author Vagisha Sharma
 * Jun 23, 2011
 */
package org.uwpr.instrumentlog.rawfile;

import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.uwpr.AdminUtils;
import org.yeastrc.project.Researcher;

/**
 * 
 */
public class ProjectUsageUpdateErrorEmailer {

	private static ProjectUsageUpdateErrorEmailer instance = new ProjectUsageUpdateErrorEmailer();
	
	private static final Logger log = Logger.getLogger(ProjectUsageUpdateErrorEmailer.class);
	
	private ProjectUsageUpdateErrorEmailer() {}
	
	public static ProjectUsageUpdateErrorEmailer getInstance() {
		return instance;
	}
	
	public void sendEmail(String errorMessage) {
		
		log.info("Sending project usage update error email");
		
		List<Researcher> researchers = AdminUtils.getNotifyAdmins();

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
			String emailStr = "";
			for(Researcher r: researchers) {
				emailStr += ","+r.getEmail();
			}
			if(emailStr.length() > 0) emailStr = emailStr.substring(1); // remove first comma

			Address[] toAddress = InternetAddress.parse(emailStr);
			message.setRecipients(Message.RecipientType.TO, toAddress);


			// set the subject
			message.setSubject("UWPR - Project usage update error!!! ");

			// set the message body
			StringBuilder text = new StringBuilder();
			text.append("There was an error while updating project usage.  The error message was: "+errorMessage+"\n");
			text.append("Please check the logs for more details.\n");
			text.append("\n");
			
			text.append("\n\nThank you,\nThe UW Proteomics Resource\n");

			System.out.println(text);
			message.setText(text.toString());

			// send the message
			Transport.send(message);

		} catch (Exception e) { log.error("Error sending email" , e); }
		
	}
}
