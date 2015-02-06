/**
 * NewPaymentMethodAddedEmailer.java
 * @author Vagisha Sharma
 * Jun 23, 2011
 */
package org.yeastrc.www.project.payment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.project.Researcher;
import org.yeastrc.project.payment.PaymentMethod;

/**
 * 
 */
public class NewPaymentMethodAddedEmailer {

	private static NewPaymentMethodAddedEmailer instance = new NewPaymentMethodAddedEmailer();
	
	private static final Logger log = Logger.getLogger(NewPaymentMethodAddedEmailer.class);
	
	private NewPaymentMethodAddedEmailer() {}
	
	public static NewPaymentMethodAddedEmailer getInstance() {
		return instance;
	}
	
	public void sendEmail(PaymentMethod paymentMethod, int projectId) {
		
		log.info("Sending new payment method email to Priska");
		
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
		
		Researcher creator = new Researcher();
		try {
			creator.load(paymentMethod.getCreatorId());
		} catch (InvalidIDException e) {
			log.error("No researcher found for ID: "+paymentMethod.getCreatorId(), e);
		} catch (SQLException e) {
			log.error("Error loading reseracher for ID: "+paymentMethod.getCreatorId(), e);
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
			message.setSubject("UWPR - A new payment method has been added to project ID " + projectId);

			// set the message body
			StringBuilder text = new StringBuilder();
			text.append("A new payment method has been added to project ID: "+projectId+"\n");
			text.append("Project URL: http://proteomicsresource.washington.edu/pr/viewProject.do?ID="+projectId+"\n");
			text.append("\n");
			
			
			text.append("Payment method ID: "+paymentMethod.getId()+"\n");
			text.append("URL: http://proteomicsresource.washington.edu/pr/viewPaymentMethod.do?projectId="+projectId+
					"&paymentMethodId="+paymentMethod.getId()+"\n");
			text.append("Created by: "+creator.getFirstName()+" "+creator.getLastName()+"\n");
			if(!StringUtils.isBlank(paymentMethod.getUwbudgetNumber())) {
				text.append("UW Budget number: "+paymentMethod.getUwbudgetNumber()+"\n");
			}
			if(!StringUtils.isBlank(paymentMethod.getPonumber())) {
				text.append("PO number: "+paymentMethod.getPonumber()+"\n");
			}
			text.append("Contact information: \n");
			text.append("Name: "+paymentMethod.getContactFirstName()+" ");
			text.append(paymentMethod.getContactLastName()+"\n");
			text.append("Email: "+paymentMethod.getContactEmail()+"\n");
			text.append("Phone: "+paymentMethod.getContactPhone()+"\n");
			
			
			text.append("\n\nThank you,\nThe UW Proteomics Resource\n");

			System.out.println(text);
			message.setText(text.toString());

			// send the message
			Transport.send(message);

		} catch (Exception e) { log.error("Error sending email" , e); }
		
	}
}
