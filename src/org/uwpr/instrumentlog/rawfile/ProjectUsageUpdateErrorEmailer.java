/**
 * ProjectUsageUpdateErrorEmailer.java
 * @author Vagisha Sharma
 * Jun 23, 2011
 */
package org.uwpr.instrumentlog.rawfile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.yeastrc.data.InvalidIDException;
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
		
		// TODO Cannot use hardcoded database IDs
		Researcher priska = new Researcher();
		try {
			priska.load(1756);
		} catch (InvalidIDException e) {
			log.error("No researcher found for ID: 1756", e);
		} catch (SQLException e) {
			log.error("Error loading reseracher for ID: 1756", e);
		}
		Researcher vsharma = new Researcher();
		try {
			vsharma.load(1811);
		} catch (InvalidIDException e) {
			log.error("No researcher found for ID: 1811", e);
		} catch (SQLException e) {
			log.error("Error loading reseracher for ID: 1811", e);
		}
		
		List<Researcher> researchers = new ArrayList<Researcher>(2);
		researchers.add(priska);
		researchers.add(vsharma);
		
		
		try {
			// set the SMTP host property value
			Properties properties = System.getProperties();
			properties.put("mail.smtp.host", "localhost");

			// create a JavaMail session
			javax.mail.Session mSession = javax.mail.Session.getInstance(properties, null);

			// create a new MIME message
			MimeMessage message = new MimeMessage(mSession);

			// set the from address
			Address fromAddress = new InternetAddress("do_not_reply@proteomicsresource.washington.edu");
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
